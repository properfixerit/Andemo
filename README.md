# Rubel App Launcher

একটি ছোট Android Home launcher, যা ইনস্টল করা `com.ijtc.rubelapp` অ্যাপটি খোলে।
এটি kiosk lock নয়; Android-এর Home app হিসেবে সেট করলেও অন্য অ্যাপে যাওয়া বন্ধ করে না।

## Debug APK

GitHub Actions-এর **Android debug APK** workflow প্রতিটি push ও pull request-এ:

1. Debug APK তৈরি করে।
2. Android emulator-এ APK ইনস্টল করে launcher চালু আছে কি না যাচাই করে।
3. `rubel-app-launcher-debug` নামে APK artifact প্রকাশ করে।

Workflow run-এর **Artifacts** অংশ থেকে APK ডাউনলোড করে ফোনে ইনস্টল করা যায়।
Rubel App আলাদাভাবে ইনস্টল থাকতে হবে; emulator smoke check launcher-টিই চালায়।
