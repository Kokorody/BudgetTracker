# Use an Ubuntu-based OpenJDK 17 image
FROM openjdk:17-slim

# Install dependencies using apt-get
RUN apt-get update && apt-get install -y wget unzip && rm -rf /var/lib/apt/lists/*

# Set environment variables for Android SDK
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin

# Download and install Android SDK command-line tools
RUN mkdir -p $ANDROID_HOME/cmdline-tools && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip -O /tmp/cmdline-tools.zip && \
    unzip /tmp/cmdline-tools.zip -d $ANDROID_HOME/cmdline-tools && \
    mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# Accept SDK licenses and install required SDK components
RUN yes | sdkmanager --licenses && \
    sdkmanager "platform-tools" "build-tools;33.0.2" "platforms;android-33"

# Set working directory
WORKDIR /workspace

# Copy project files into the container
COPY . /workspace

# Pre-download Gradle wrapper to cache Gradle distribution
RUN ./gradlew --version

# Optional: Pre-build the project
RUN ./gradlew build
