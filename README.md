# android mkey generator

PSA: This is mostly vibe coded, yes i know vibe coding is bad, but it works and im lazy im sorry.

mkey generator is a simple and user-friendly Android application designed to generate master keys for various Nintendo devices. It provides a clean, modern interface for inputting system information and retrieving the corresponding master key from mkey.salthax.org.

## Application Process

1. First, the app gathers all your input from the interface (device, date, inquiry number).
2. It then sends this data to the website's server in a POST request.
3. The server processes your data to generate the master key, sending it back to the app inside an HTML page.
4. Finally, the app parses the HTML, extracts the key, and displays it on your screen.

## Installation & Usage

1.  Download the latest APK from the [releases page](https://github.com/aeolhcia/Mkey-Gen-App/releases).
2.  Enable "Install from unknown sources" in your device settings.
3.  Install the APK on your Android device.
4.  Open the app, select your device, enter the system date and inquiry number, and tap "Generate Key" to retrieve your master key.

---

## FAQ

Q: What is the point of this? Why can't you just use the website?

A: This tool is mostly intended for people who clean and refurbish consoles all day. The website is fully functional and good enough for 99% of people. However, its ui can be pretty annoying to use on mobile, and when reseting lots of devices in a row having a easier to use tool is nice to have!

---

## Screenshots

<img src="/imgs/screenshot01.png" width="300">  <img src="/imgs/screenshot03.png" width="300">  <img src="/imgs/screenshot02.png" width="300"> 

---

## Credits

* Mkey by Dazjo https://github.com/dazjo/mkey
* Server-side processing by salthax https://mkey.salthax.org

---

## License

This project is licensed under the MIT License.
