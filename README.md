# 🚗 Smart Indoor Parking System (Android)

Welcome to the **Smart Indoor Parking System** project! This Android-based solution is designed to streamline parking management in indoor parking lots, providing real-time updates on available spots, guiding users to free spaces, and ensuring efficient space utilization.

## Table of Contents

- [About the Project](#about-the-project)
- [Team Members](#team-members)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [License](#license)
- [Contact](#contact)

## About the Project

The **Smart Indoor Parking System** Android application is designed to assist drivers in finding available parking spots in indoor parking facilities using smart technologies like sensors and real-time data. It aims to reduce the time spent looking for parking spots, optimize the use of parking spaces, and provide a user-friendly interface for drivers.

By leveraging IoT sensors and an Android app, the system provides real-time parking availability, efficient guidance, and navigation to available parking spaces.

### Goals:
- Minimize time spent searching for parking spaces.
- Maximize efficient usage of parking facilities.
- Provide real-time parking availability to users.

GitHub Repository: [Smart Indoor Parking System](https://github.com/RushiPatel9144/SmartIndoorParkingSystem.git)


## Team Members

1. **Kunal Dhiman** - n01540952
2. **Nisargkumar Pareshbhai Joshi** - n01545986
3. **Rushi Manojkumar Patel** - N01539144
4. **Raghav Sharma** - n01537255
5. 
## Key Features

- 🚦 **Real-Time Parking Availability**: Displays available and occupied parking spaces in real-time.
- 🗺️ **Navigation Assistance**: Guides users to the nearest available parking spot.
- 📊 **Data Analytics**: Collects parking usage data for facility managers to optimize operations.
- 📱 **Android App Integration**: Users can check parking availability and navigate through the parking facility via the Android app.

## Tech Stack

- **Frontend**: Android Studio (Java/Kotlin)
- **Backend**: Firebase (for real-time database)
- **IoT Devices**: Raspberry Pi with Ultrasonic Sensors for real-time parking spot detection with four different dedicated sensors.
- **Cloud Services**: Firebase Realtime Database (for storing parking spot information)

## System Architecture

The system is divided into several components:

1. **IoT Sensors**: Each parking spot is equipped with sensors (e.g., ultrasonic,gas,etc) to detect if the spot is occupied.
2. **Data Processing**: A central server (Raspberry Pi) collects data from the sensors and sends it to the Firebase database.
3. **Android App**: The Android app communicates with Firebase to fetch real-time parking availability and guides users to available spots.

![System Architecture - coming soon](https://example.com/architecture-diagram.png) <!-- Replace with your actual architecture diagram -->




## License

Distributed under the MIT License. See `LICENSE` for more information.


## Contact

For any inquiries or support, feel free to reach out:

- Project Lead: [Raghav Sharma](https://github.com/RaghavSharma7255)
- Email: n01537255@students.humber.ca
- Supervised under: [Haki Sharifi](https://github.com/Haki11)
