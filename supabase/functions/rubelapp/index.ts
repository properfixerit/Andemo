const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, PUT, POST, DELETE, OPTIONS",
  "Access-Control-Expose-Headers": "X-GitHub-Content-SHA",
};

const allowedFiles = new Map<string, {
  owner: string;
  repo: string;
  path: string;
  writable: boolean;
  allowEmpty: boolean;
}>([
  ["QuestionList", {
    owner: "properfixerit",
    repo: "RubelApp",
    path: "QuestionList",
    writable: true,
    allowEmpty: false,
  }],
  ["OnTest", {
    owner: "properfixerit",
    repo: "RubelApp",
    path: "OnTest",
    writable: false,
    allowEmpty: false,
  }],
  ...[
    "BankList",
    "ContactList",
    "EntryList",
    "FileList",
    "ItemList",
    "ProductList",
    "RPList",
    "TypeList",
    "UnitList",
    "UserList",
  ].map((path) => [path, {
    owner: "properfixerit",
    repo: "RubelApp",
    path,
    writable: true,
    allowEmpty: true,
  }] as const),
]);
const publicWritableFiles = new Set([
  "BankList",
  "ContactList",
  "EntryList",
  "FileList",
  "ItemList",
  "ProductList",
  "TypeList",
  "UnitList",
  "UserList",
]);
const maxFileBytes = 2 * 1024 * 1024;
const maxAttachmentBytes = 5 * 1024 * 1024;

function jsonResponse(status: number, message: string): Response {
  return new Response(JSON.stringify({ message }), {
    status,
    headers: {
      ...corsHeaders,
      "Cache-Control": "no-store",
      "Content-Type": "application/json; charset=utf-8",
    },
  });
}

function isSupabaseApiKeyAllowed(request: Request, anonKey: string): boolean {
  const apiKey = request.headers.get("apikey");
  if (apiKey === anonKey) return true;
  if (apiKey === null) return false;

  const configuredKeys = Deno.env.get("SUPABASE_PUBLISHABLE_KEYS");
  if (!configuredKeys) return false;
  const publishableKeys: unknown = JSON.parse(configuredKeys);
  if (typeof publishableKeys !== "object" || publishableKeys === null ||
    Array.isArray(publishableKeys)) {
    return false;
  }
  return Object.values(publishableKeys).some((key) => key === apiKey);
}

function githubHeaders(token: string): HeadersInit {
  return {
    "Accept": "application/vnd.github+json",
    "Authorization": `Bearer ${token}`,
    "User-Agent": "RubelApp-Supabase-Edge-Function",
    "X-GitHub-Api-Version": "2022-11-28",
  };
}

async function isAllowedAdmin(request: Request, anonKey: string): Promise<boolean> {
  const authorization = request.headers.get("Authorization");
  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  if (!authorization?.startsWith("Bearer ") || !supabaseUrl) return false;

  const userResponse = await fetch(`${supabaseUrl}/auth/v1/user`, {
    headers: {
      "apikey": anonKey,
      "Authorization": authorization,
    },
    signal: AbortSignal.timeout(10_000),
  });
  if (!userResponse.ok) return false;
  const user = await userResponse.json();
  return typeof user.email === "string" &&
    user.email.toLowerCase() === "properfixer.it@gmail.com";
}

function encodeBase64(value: string): string {
  const bytes = new TextEncoder().encode(value);
  let binary = "";
  for (let offset = 0; offset < bytes.length; offset += 0x8000) {
    binary += String.fromCharCode(...bytes.subarray(offset, offset + 0x8000));
  }
  return btoa(binary);
}

function encodeBase64Bytes(bytes: Uint8Array): string {
  let binary = "";
  for (let offset = 0; offset < bytes.length; offset += 0x8000) {
    binary += String.fromCharCode(...bytes.subarray(offset, offset + 0x8000));
  }
  return btoa(binary);
}

function attachmentUploadError(status: number, githubMessage: string): string {
  const normalizedMessage = githubMessage.toLowerCase();
  if (normalizedMessage.includes("repository is empty") ||
    normalizedMessage.includes("git repository is empty")) {
    return "RubelApp has no commits yet. Add an initial file (for example README.md) on main, then retry the upload.";
  }
  if (status === 401 || status === 403) {
    return "GitHub rejected the upload. Check that GITHUB_REPO_WRITE_TOKEN has Contents: Read and write access to properfixerit/RubelApp.";
  }
  if (status === 404) {
    return "GitHub could not find RubelApp or the main branch. Initialize the main branch and check the write token's repository access.";
  }
  if (status === 409) {
    return "RubelApp still changed during upload after an automatic retry. Check for another upload or repository update, then try again.";
  }
  if (status === 422) {
    return "GitHub rejected the file. Initialize RubelApp's main branch and verify the file path and write-token permission.";
  }
  return `GitHub could not upload the file (HTTP ${status}).`;
}

Deno.serve(async (request: Request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }
  if (request.method !== "GET" && request.method !== "PUT" &&
    request.method !== "POST" && request.method !== "DELETE") {
    return jsonResponse(405, "Method not allowed.");
  }

  const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
  if (!anonKey) {
    console.error("RubelApp proxy is missing its Supabase anon key.");
    return jsonResponse(500, "Private exam data is not configured.");
  }
  if (!isSupabaseApiKeyAllowed(request, anonKey)) {
    return jsonResponse(401, "Unauthorized.");
  }

  const requestUrl = new URL(request.url);
  const file = requestUrl.searchParams.get("file");
  if (file === "FileAttachment") {
    const directory = requestUrl.searchParams.get("directory") ?? "FileList";
    const fileId = requestUrl.searchParams.get("id") ?? "";
    const name = requestUrl.searchParams.get("name") ?? "";
    if ((directory !== "FileData" && directory !== "FileList") ||
      !/^[A-Za-z0-9_-]{1,64}$/.test(fileId) ||
      !/^[A-Za-z0-9][A-Za-z0-9._ -]{0,119}$/.test(name) ||
      name === "." || name === "..") {
      return jsonResponse(400, "A valid FileList ID and file name are required.");
    }
    const path = `${directory}/${fileId}/${name}`;
    const attachmentUrl = new URL(
      `https://api.github.com/repos/properfixerit/RubelApp/contents/${
        path.split("/").map(encodeURIComponent).join("/")
      }`,
    );
    attachmentUrl.searchParams.set("ref", "main");

    if (request.method === "DELETE") {
      // This function is dedicated to the RubelApp repository; attachment deletion is public.
      const githubWriteToken = Deno.env.get("GITHUB_REPO_WRITE_TOKEN");
      if (!githubWriteToken) {
        console.error("FileList attachment deletion is missing its GitHub write secret.");
        return jsonResponse(500, "File deletion is not configured.");
      }
      let currentAttachment: Response;
      try {
        currentAttachment = await fetch(attachmentUrl, {
          headers: githubHeaders(githubWriteToken),
          signal: AbortSignal.timeout(20_000),
        });
      } catch (error) {
        console.error(`Could not check the FileList attachment before deleting ${path}:`, error);
        return jsonResponse(502, "Could not connect to the RubelApp repository.");
      }
      if (currentAttachment.status === 404) {
        return new Response(JSON.stringify({
          message: "Attachment was already absent from RubelApp.",
        }), {
          headers: {
            ...corsHeaders,
            "Cache-Control": "no-store",
            "Content-Type": "application/json; charset=utf-8",
          },
        });
      }
      if (!currentAttachment.ok) {
        console.error(
          `GitHub returned HTTP ${currentAttachment.status} while checking ${path} for deletion.`,
        );
        return jsonResponse(
          502,
          currentAttachment.status === 401 || currentAttachment.status === 403
            ? "GitHub rejected the delete. Check that GITHUB_REPO_WRITE_TOKEN has Contents: Read and write access to properfixerit/RubelApp."
            : "Could not check the attachment in the RubelApp repository.",
        );
      }
      let attachmentSha: string;
      try {
        const metadata = await currentAttachment.json();
        if (typeof metadata.sha !== "string") {
          return jsonResponse(502, "GitHub returned an invalid attachment revision.");
        }
        attachmentSha = metadata.sha;
      } catch (error) {
        console.error(`Could not read the GitHub attachment revision for ${path}:`, error);
        return jsonResponse(502, "Could not read the attachment revision from GitHub.");
      }

      let deleteResponse: Response;
      try {
        deleteResponse = await fetch(attachmentUrl, {
          method: "DELETE",
          headers: githubHeaders(githubWriteToken),
          body: JSON.stringify({
            message: `Delete FileList attachment ${name}`,
            sha: attachmentSha,
            branch: "main",
          }),
          signal: AbortSignal.timeout(30_000),
        });
      } catch (error) {
        console.error(`Could not delete FileList attachment ${path} from GitHub:`, error);
        return jsonResponse(502, "Could not connect to the RubelApp repository.");
      }
      if (!deleteResponse.ok) {
        let githubMessage = "";
        try {
          const errorPayload = await deleteResponse.json();
          if (typeof errorPayload.message === "string") {
            githubMessage = errorPayload.message;
          }
        } catch (error) {
          console.error(`Could not read GitHub's deletion error for ${path}:`, error);
        }
        console.error(
          `GitHub returned HTTP ${deleteResponse.status} while deleting ${path}: ${
            githubMessage || "No error detail returned."
          }`,
        );
        return jsonResponse(
          deleteResponse.status === 409 ? 409 : 502,
          deleteResponse.status === 401 || deleteResponse.status === 403
            ? "GitHub rejected the delete. Check that GITHUB_REPO_WRITE_TOKEN has Contents: Read and write access to properfixerit/RubelApp."
            : deleteResponse.status === 409
            ? "The attachment changed during deletion. Try deleting the FileList record again."
            : `Could not delete the GitHub attachment (HTTP ${deleteResponse.status}).`,
        );
      }
      return new Response(JSON.stringify({
        message: "Attachment deleted from RubelApp.",
      }), {
        headers: {
          ...corsHeaders,
          "Cache-Control": "no-store",
          "Content-Type": "application/json; charset=utf-8",
        },
      });
    }
    if (request.method !== "GET") {
      return jsonResponse(405, "Method not allowed.");
    }
    const githubReadToken = Deno.env.get("GITHUB_REPO_READ_TOKEN");
    if (!githubReadToken) {
      console.error("FileList attachment download is missing its GitHub read secret.");
      return jsonResponse(500, "File download is not configured.");
    }
    let attachmentResponse: Response;
    try {
      attachmentResponse = await fetch(attachmentUrl, {
        headers: {
          ...githubHeaders(githubReadToken),
          "Accept": "application/vnd.github.raw+json",
        },
        signal: AbortSignal.timeout(30_000),
      });
    } catch (error) {
      console.error(`Could not download FileList attachment ${path}:`, error);
      return jsonResponse(502, "Could not connect to the private RubelApp repository.");
    }
    if (!attachmentResponse.ok) {
      console.error(
        `GitHub returned HTTP ${attachmentResponse.status} while downloading ${path}.`,
      );
      return jsonResponse(
        attachmentResponse.status === 404 ? 404 : 502,
        attachmentResponse.status === 404
          ? "The file was not found in RubelApp."
          : "Could not download the file from RubelApp.",
      );
    }
    const declaredLength = Number(attachmentResponse.headers.get("Content-Length") ?? "0");
    if (declaredLength > maxAttachmentBytes) {
      return jsonResponse(413, "Files must be 5 MiB or smaller.");
    }
    let bytes: Uint8Array;
    try {
      bytes = new Uint8Array(await attachmentResponse.arrayBuffer());
    } catch (error) {
      console.error(`Could not read FileList attachment ${path} from GitHub:`, error);
      return jsonResponse(502, "Could not read the file from RubelApp.");
    }
    if (bytes.length === 0 || bytes.length > maxAttachmentBytes) {
      return jsonResponse(
        bytes.length > maxAttachmentBytes ? 413 : 404,
        bytes.length > maxAttachmentBytes
          ? "Files must be 5 MiB or smaller."
          : "The repository returned an empty file.",
      );
    }
    return new Response(bytes, {
      headers: {
        ...corsHeaders,
        "Cache-Control": "no-store",
        "Content-Type": attachmentResponse.headers.get("Content-Type") ??
          "application/octet-stream",
        "Content-Disposition": `attachment; filename="${name.replace(/["\\]/g, "_")}"`,
      },
    });
  }

  const fileConfig = file ? allowedFiles.get(file) : undefined;
  if (!file || !fileConfig) {
    return jsonResponse(400, "Unsupported exam question file.");
  }
  if (request.method === "DELETE") {
    return jsonResponse(405, "Method not allowed.");
  }
  const githubUrl = new URL(
    `https://api.github.com/repos/${fileConfig.owner}/${fileConfig.repo}/contents/${fileConfig.path}`,
  );
  githubUrl.searchParams.set("ref", "main");

  if (request.method === "POST") {
    if (file !== "FileList") {
      return jsonResponse(400, "Attachments can only be uploaded to FileList.");
    }

    const fileId = new URL(request.url).searchParams.get("id") ?? "";
    const name = new URL(request.url).searchParams.get("name") ?? "";
    const directory = new URL(request.url).searchParams.get("directory") ?? "FileData";
    if (!/^[A-Za-z0-9_-]{1,64}$/.test(fileId) ||
      directory !== "FileData" ||
      !/^[A-Za-z0-9][A-Za-z0-9._ -]{0,119}$/.test(name) ||
      name === "." || name === "..") {
      return jsonResponse(400, "A valid FileList ID and file name are required.");
    }
    const declaredLength = Number(request.headers.get("Content-Length") ?? "0");
    if (declaredLength > maxAttachmentBytes) {
      return jsonResponse(413, "Files must be 5 MiB or smaller.");
    }
    let bytes: Uint8Array;
    try {
      bytes = new Uint8Array(await request.arrayBuffer());
    } catch (error) {
      console.error("Could not read the uploaded FileList attachment:", error);
      return jsonResponse(400, "Could not read the uploaded file.");
    }
    if (bytes.length === 0) {
      return jsonResponse(400, "The uploaded file is empty.");
    }
    if (bytes.length > maxAttachmentBytes) {
      return jsonResponse(413, "Files must be 5 MiB or smaller.");
    }

    const githubWriteToken = Deno.env.get("GITHUB_REPO_WRITE_TOKEN");
    if (!githubWriteToken) {
      console.error("FileList attachment upload is missing its GitHub write secret.");
      return jsonResponse(500, "File upload is not configured.");
    }
    const path = `FileData/${fileId}/${name}`;
    const attachmentUrl = new URL(
      `https://api.github.com/repos/properfixerit/RubelApp/contents/${
        path.split("/").map(encodeURIComponent).join("/")
      }`,
    );
    attachmentUrl.searchParams.set("ref", "main");

    let currentAttachment: Response;
    try {
      currentAttachment = await fetch(attachmentUrl, {
        headers: githubHeaders(githubWriteToken),
        signal: AbortSignal.timeout(20_000),
      });
    } catch (error) {
      console.error(`Could not check the existing FileList attachment ${path}:`, error);
      return jsonResponse(502, "Could not connect to the private file repository.");
    }
    let currentSha: string | undefined;
    if (currentAttachment.ok) {
      const metadata = await currentAttachment.json();
      if (typeof metadata.sha !== "string") {
        return jsonResponse(502, "GitHub returned an invalid existing file revision.");
      }
      currentSha = metadata.sha;
    } else if (currentAttachment.status !== 404) {
      console.error(
        `GitHub returned HTTP ${currentAttachment.status} while checking ${path}.`,
      );
      return jsonResponse(502, "Could not check the existing file in the repository.");
    }

    const uploadAttachment = (sha: string | undefined) =>
      fetch(attachmentUrl, {
        method: "PUT",
        headers: githubHeaders(githubWriteToken),
        body: JSON.stringify({
          message: `Upload FileList attachment ${name}`,
          content: encodeBase64Bytes(bytes),
          ...(sha ? { sha } : {}),
          branch: "main",
        }),
        signal: AbortSignal.timeout(60_000),
      });

    let uploadResponse: Response;
    try {
      uploadResponse = await uploadAttachment(currentSha);
      if (uploadResponse.status === 409) {
        let latestAttachment: Response;
        try {
          latestAttachment = await fetch(attachmentUrl, {
            headers: githubHeaders(githubWriteToken),
            signal: AbortSignal.timeout(20_000),
          });
        } catch (error) {
          console.error(`Could not refresh the FileData attachment revision for ${path}:`, error);
          return jsonResponse(502, "Could not reconnect to RubelApp after an upload conflict.");
        }
        if (latestAttachment.ok) {
          let metadata: { sha?: unknown };
          try {
            metadata = await latestAttachment.json();
          } catch (error) {
            console.error(`Could not read the refreshed GitHub attachment revision for ${path}:`, error);
            return jsonResponse(502, "Could not read the attachment revision after an upload conflict.");
          }
          if (typeof metadata.sha !== "string") {
            return jsonResponse(502, "GitHub returned an invalid attachment revision after a conflict.");
          }
          currentSha = metadata.sha;
        } else if (latestAttachment.status === 404) {
          currentSha = undefined;
        } else {
          console.error(
            `GitHub returned HTTP ${latestAttachment.status} while refreshing ${path} after a conflict.`,
          );
          return jsonResponse(502, "Could not refresh the FileData attachment after an upload conflict.");
        }

        uploadResponse = await uploadAttachment(currentSha);
      }
    } catch (error) {
      console.error(`Could not upload FileData attachment ${path}:`, error);
      return jsonResponse(502, "Could not connect to the RubelApp repository.");
    }
    if (!uploadResponse.ok) {
      let githubMessage = "";
      try {
        const errorPayload = await uploadResponse.json();
        if (typeof errorPayload.message === "string") {
          githubMessage = errorPayload.message;
        }
      } catch (error) {
        console.error(`Could not read GitHub's error response for ${path}:`, error);
      }
      console.error(
        `GitHub returned HTTP ${uploadResponse.status} while uploading ${path}: ${
          githubMessage || "No error detail returned."
        }`,
      );
      return jsonResponse(
        uploadResponse.status === 409 ? 409 : 502,
        attachmentUploadError(uploadResponse.status, githubMessage),
      );
    }
    const uploadedMetadata = await uploadResponse.json();
    if (typeof uploadedMetadata.content?.sha !== "string") {
      return jsonResponse(502, "GitHub did not confirm the uploaded file.");
    }
    return new Response(JSON.stringify({
      message: "File uploaded to RubelApp.",
      path,
    }), {
      headers: {
        ...corsHeaders,
        "Cache-Control": "no-store",
        "Content-Type": "application/json; charset=utf-8",
      },
    });
  }

  if (request.method === "PUT") {
    if (!fileConfig.writable) {
      return jsonResponse(400, "This question file cannot be updated.");
    }
    if (!publicWritableFiles.has(file)) {
      let allowedAdmin: boolean;
      try {
        allowedAdmin = await isAllowedAdmin(request, anonKey);
      } catch (error) {
        console.error(`Could not verify the ${file} editor account:`, error);
        return jsonResponse(502, "Could not verify the administrator session.");
      }
      if (!allowedAdmin) {
        return jsonResponse(403, "Admin access required.");
      }
    }

    const githubWriteToken = Deno.env.get("GITHUB_REPO_WRITE_TOKEN");
    if (!githubWriteToken) {
      console.error("Question editing is missing its GitHub write secret.");
      return jsonResponse(500, "Question editing is not configured.");
    }

    let update: { content?: unknown; sha?: unknown };
    try {
      update = await request.json();
    } catch {
      return jsonResponse(400, "Invalid question update request.");
    }
    if (typeof update.content !== "string" || typeof update.sha !== "string") {
      return jsonResponse(400, "Question content and file revision are required.");
    }
    if ((!update.content.trim() && !fileConfig.allowEmpty) ||
      new TextEncoder().encode(update.content).length > maxFileBytes) {
      return jsonResponse(400, "Question content must be non-empty and no larger than 2 MiB.");
    }

    let currentFile: Response;
    try {
      currentFile = await fetch(githubUrl, {
        headers: githubHeaders(githubWriteToken),
        signal: AbortSignal.timeout(20_000),
      });
    } catch (error) {
      console.error(`Could not check the current ${file} file revision:`, error);
      return jsonResponse(502, "Could not connect to the private exam repository.");
    }
    if (!currentFile.ok) {
      console.error(`GitHub returned HTTP ${currentFile.status} while checking ${file} revision.`);
      return jsonResponse(502, `Could not load the current ${file} file.`);
    }
    const currentMetadata = await currentFile.json();
    if (typeof currentMetadata.sha !== "string") {
      return jsonResponse(502, `GitHub returned an invalid ${file} file revision.`);
    }
    if (currentMetadata.sha !== update.sha) {
      return jsonResponse(409, `The ${file} file changed elsewhere. Refresh and try again.`);
    }

    let updateResponse: Response;
    try {
      updateResponse = await fetch(githubUrl, {
        method: "PUT",
        headers: githubHeaders(githubWriteToken),
        body: JSON.stringify({
          message: `Update ${file} via RubelApp admin`,
          content: encodeBase64(update.content),
          sha: currentMetadata.sha,
          branch: "main",
        }),
        signal: AbortSignal.timeout(20_000),
      });
    } catch (error) {
      console.error(`Could not update ${file} in GitHub:`, error);
      return jsonResponse(502, `Could not save ${file} to the repository.`);
    }
    if (!updateResponse.ok) {
      console.error(`GitHub returned HTTP ${updateResponse.status} while saving ${file}.`);
      return jsonResponse(
        updateResponse.status === 409 ? 409 : 502,
        updateResponse.status === 409
          ? `The ${file} file changed elsewhere. Refresh and try again.`
          : `Could not save ${file} to the repository.`,
      );
    }
    const updatedMetadata = await updateResponse.json();
    if (typeof updatedMetadata.content?.sha !== "string") {
      return jsonResponse(502, `GitHub did not confirm the saved ${file} revision.`);
    }
    return new Response(JSON.stringify({
      message: `${file} saved.`,
      sha: updatedMetadata.content.sha,
    }), {
      headers: {
        ...corsHeaders,
        "Cache-Control": "no-store",
        "Content-Type": "application/json; charset=utf-8",
      },
    });
  }

  const githubReadToken = Deno.env.get("GITHUB_REPO_READ_TOKEN");
  if (!githubReadToken) {
    console.error("RubelApp proxy is missing its GitHub read secret.");
    return jsonResponse(500, "Private exam data is not configured.");
  }

  let githubResponse: Response;
  try {
    githubResponse = await fetch(githubUrl, {
      headers: githubHeaders(githubReadToken),
      signal: AbortSignal.timeout(20_000),
    });
  } catch (error) {
    console.error("Private GitHub question fetch failed:", error);
    return jsonResponse(502, "Could not connect to the private exam repository.");
  }

  if (!githubResponse.ok) {
    console.error(
      `GitHub returned HTTP ${githubResponse.status} for the ${file} question file.`,
    );
    return jsonResponse(502, "Could not load the private exam question file.");
  }

  const contentLength = Number(githubResponse.headers.get("Content-Length") ?? "0");
  if (contentLength > maxFileBytes * 2) {
    return jsonResponse(413, "The exam question file is too large.");
  }

  let content: string;
  let fileSha = "";
  try {
    const payload = await githubResponse.json();
    if (payload.truncated === true || typeof payload.content !== "string") {
      return jsonResponse(502, "GitHub returned an unsupported question file.");
    }
    if (typeof payload.sha === "string") fileSha = payload.sha;
    const binary = atob(payload.content.replace(/\s/g, ""));
    if (binary.length > maxFileBytes) {
      return jsonResponse(413, "The exam question file is too large.");
    }
    const bytes = Uint8Array.from(binary, (character) => character.charCodeAt(0));
    content = new TextDecoder("utf-8", { fatal: true }).decode(bytes);
  } catch (error) {
    console.error(`Could not decode the ${file} question file:`, error);
    return jsonResponse(502, "Could not decode the private exam question file.");
  }

  if (!content.trim() && !fileConfig.allowEmpty) {
    return jsonResponse(502, "The exam question file is empty.");
  }

  return new Response(content, {
    headers: {
      ...corsHeaders,
      "X-GitHub-Content-SHA": fileSha,
      "Cache-Control": "no-store",
      "Content-Type": "text/plain; charset=utf-8",
    },
  });
});
