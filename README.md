# Call Blocker App

Call Blocker App is a Kotlin-based application designed to help you manage and block unwanted incoming calls while maintaining a whitelist of approved contacts. It integrates multiple spam detection sources, including real-time web scraping and an external API with multi-country support.

## Screenshots

Here are some screenshots of the Call Blocker App in action:

<div style="display: flex; gap: 10px;">
  <img src="https://i.imgur.com/g8Ie7zU.png" alt="Main screen" width="300">  
  <img src="https://i.imgur.com/QnbalOh.png" alt="Phone number options" width="300">  
  <img src="https://i.imgur.com/QeC3Kg8.png" alt="Settings" width="300">  
  <img src="https://i.imgur.com/I4tDolO.png" alt="More settings" width="300">
  <img src="https://i.imgur.com/T2DdbPY.png" alt="Report dialog" width="300">
</div>

## Features

- **Block and Unblock Numbers**: Easily block or unblock specific phone numbers.
- **Whitelist Management**: Maintain a whitelist of phone numbers that are always allowed to reach you.
- **Advanced Blocking Options**: Multiple blocking criteria available:
  - Block calls from hidden numbers
  - Block calls from numbers not in contacts
  - Block international calls
- **International Phone Number Lookup**: Multi-source verification using:
  - UnknownPhone API
  - Tellows API
  - TrueCaller API
- **Web-Based Number Lookup**: Automatically look up phone numbers using trusted Spanish websites:
  - [www.listaspam.com](https://www.listaspam.com)
  - [www.responderono.es](https://www.responderono.es)
  - [www.cleverdialer.es](https://www.cleverdialer.es)
- **STIR/SHAKEN Verification**: Support for blocking calls based on STIR/SHAKEN levels.
- **Auto-Mute Option**: Choose to automatically mute incoming spam calls instead of blocking them completely.
- **Report Dialog**: Built-in system to report suspicious or safe numbers.
- **Automatic App Updates**: The app checks for updates upon launch and prompts the user to install them if available.

## Contribution

We welcome contributions of any kind — code, documentation, bug reports, or feature suggestions.

If you find this project helpful and want to support its development, consider making a donation.

Your support helps keep the project active and maintained. Thank you! 🙌

<div style="display: flex; gap: 10px;">
  <a target="_blank" href="https://www.buymeacoffee.com/rSiZtB3"><img style="height: 50px" src="https://i.imgur.com/KCk0bxY.png" /></a>
  <a target="_blank" href="https://www.paypal.com/donate/?hosted_button_id=3T9XNAPWW36Z2"><img style="height: 50px" src="https://i.imgur.com/Z3x38ey.png" /></a>
</div>


## Data Management

- **Export**: Users can export all their preferences, including settings and black/white lists of numbers, to a file for backup or transfer to another device.
- **Import**: Previously exported data can be imported, allowing users to quickly set up the app on a new device or restore settings after a reset.

## Privacy and Security

This app is designed with user privacy in mind. All data is stored locally on the device, and the app only accesses the internet to perform API requests and web scraping for number lookup. No personal data is shared with external servers.

## Requirements

- Android 10.0 or higher
- Internet connection for API requests and web scraping features

## Installation

1. Download the APK from the releases page or compile it by yourself.
2. Enable installation from unknown sources in your device settings.
3. Install the app and grant necessary permissions for call management and internet access.

## Configuration

Before building the project, set the following environment variables with your API keys:

- `UNKNOWN_PHONE_API_KEY`
- `TELLOWS_API_KEY`
- `TRUECALLER_API_KEY`

These keys are required for spam lookups and reporting features.

## License

This app is released under the GPLv3 License. See the LICENSE file for more details.
