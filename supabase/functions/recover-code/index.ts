import { createClient } from "npm:@supabase/supabase-js@2";
import nodemailer from "npm:nodemailer@6.10.1";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const genericMessage =
  "If this device is registered, a recovery email request was processed.";

function jsonResponse(status: number, body: Record<string, unknown>): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

Deno.serve(async (request: Request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }
  if (request.method !== "POST") {
    return jsonResponse(405, { success: false, message: "Method not allowed." });
  }

  const projectUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
  if (!projectUrl || !serviceRoleKey || !anonKey) {
    console.error("Required Supabase function environment variables are missing.");
    return jsonResponse(500, {
      success: false,
      message: "The recovery service is not configured.",
    });
  }
  if (request.headers.get("apikey") !== anonKey) {
    return jsonResponse(401, { success: false, message: "Unauthorized." });
  }

  let deviceId: string;
  try {
    const body = await request.json();
    deviceId = typeof body.deviceId === "string" ? body.deviceId.trim() : "";
  } catch {
    return jsonResponse(400, { success: false, message: "Invalid request body." });
  }
  if (!deviceId || deviceId.length > 128) {
    return jsonResponse(400, { success: false, message: "Invalid device ID." });
  }

  const supabase = createClient(projectUrl, serviceRoleKey, {
    auth: { autoRefreshToken: false, persistSession: false },
  });
  const { data: user, error: userError } = await supabase
    .from("pf_exam_users")
    .select("time_id,email")
    .eq("device_id", deviceId)
    .maybeSingle();
  if (userError) {
    console.error("Recovery account lookup failed:", userError.message);
    return jsonResponse(500, {
      success: false,
      message: "Could not request a recovery email. Please try again later.",
    });
  }
  if (!user) {
    return jsonResponse(200, { success: true, message: genericMessage });
  }

  const smtpHost = Deno.env.get("SMTP_HOST");
  const smtpPort = Number(Deno.env.get("SMTP_PORT") ?? "587");
  const smtpUser = Deno.env.get("SMTP_USER");
  const smtpPassword = Deno.env.get("SMTP_PASSWORD");
  const emailFrom = Deno.env.get("SMTP_FROM");
  if (!smtpHost || !smtpUser || !smtpPassword || !emailFrom ||
    !Number.isInteger(smtpPort) || smtpPort < 1 || smtpPort > 65535) {
    console.error("SMTP environment variables are missing or invalid.");
    return jsonResponse(500, {
      success: false,
      message: "The recovery email service is not configured.",
    });
  }

  const { data: allowed, error: limitError } = await supabase.rpc(
    "claim_pf_exam_code_recovery",
    { p_device_id: deviceId },
  );
  if (limitError) {
    console.error("Recovery rate limit check failed:", limitError.message);
    return jsonResponse(500, {
      success: false,
      message: "Could not request a recovery email. Please try again later.",
    });
  }
  if (!allowed) {
    const { data: limit, error: lookupError } = await supabase
      .from("pf_exam_code_recovery_limits")
      .select("last_requested_at")
      .eq("device_id", deviceId)
      .maybeSingle();
    if (lookupError) {
      console.error("Recovery cooldown lookup failed:", lookupError.message);
      return jsonResponse(429, {
        success: false,
        message: "A recovery email was requested recently. Please try again shortly.",
      });
    }
    const lastRequestedAt = limit?.last_requested_at
      ? Date.parse(limit.last_requested_at)
      : Number.NaN;
    const retryAfterSeconds = Number.isFinite(lastRequestedAt)
      ? Math.max(1, Math.ceil((lastRequestedAt + 60_000 - Date.now()) / 1000))
      : 60;
    return jsonResponse(429, {
      success: false,
      retryAfterSeconds,
      message: `A recovery email was sent recently. Please try again in ${retryAfterSeconds} seconds.`,
    });
  }

  try {
    const transporter = nodemailer.createTransport({
      host: smtpHost,
      port: smtpPort,
      secure: smtpPort === 465,
      auth: { user: smtpUser, pass: smtpPassword },
    });
    const delivery = await transporter.sendMail({
      from: emailFrom,
      to: user.email,
      subject: "RubelApp login code recovery",
      text: `Your RubelApp login code is ${user.time_id}. Keep this code private.`,
    });
    const acceptedRecipients = delivery.accepted.map((recipient) =>
      typeof recipient === "string" ? recipient : recipient.address
    );
    if (!acceptedRecipients.some(
      (address) => address.toLowerCase() === user.email.toLowerCase()
    )) {
      throw new Error("SMTP did not accept the recovery email recipient.");
    }
    return jsonResponse(200, {
      success: true,
      message: "Recovery email accepted for delivery.",
      recipientEmail: user.email,
    });
  } catch (error) {
    console.error("Recovery email delivery failed:", error);
    const { error: releaseError } = await supabase
      .from("pf_exam_code_recovery_limits")
      .delete()
      .eq("device_id", deviceId);
    if (releaseError) {
      console.error("Could not release the recovery cooldown:", releaseError.message);
    }
    return jsonResponse(502, {
      success: false,
      message: "Could not send the recovery email. Please try again later.",
    });
  }
});
