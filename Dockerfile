# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml first to cache dependency downloads as a separate layer.
# Maven re-downloads dependencies only when pom.xml changes, not on every
# source edit — this keeps incremental builds fast.
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -q -DskipTests

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
# Use JRE (not JDK) to keep the final image lean (~200 MB vs ~400 MB).
FROM eclipse-temurin:17-jre

WORKDIR /app

# Install X11 libraries required by Swing/AWT to render the GUI.
RUN apt-get update && apt-get install -y --no-install-recommends \
    libxext6 libxrender1 libxtst6 libxi6 libx11-6 \
    && rm -rf /var/lib/apt/lists/*

# Copy only the shaded fat-JAR from the build stage; no Maven/JDK in the image.
COPY --from=builder /build/target/dental-reservation-1.0-SNAPSHOT.jar app.jar

# Copy image assets loaded by the Swing UI at runtime.
COPY images/ ./images/

# Run as a non-root user — security best practice for containerized apps.
RUN useradd -m appuser && chown -R appuser /app
USER appuser

# dental.db is created in WORKDIR at runtime. For data persistence across
# container restarts, bind-mount the file:
#   docker run -v "%cd%\dental.db:/app/dental.db" ...   (Windows)
#   docker run -v "$(pwd)/dental.db:/app/dental.db" ...  (Linux/macOS)

# This app has a Swing GUI. To display it on the host run:
#   Windows (VcXsrv):  docker run -e DISPLAY=host.docker.internal:0.0 ...
#   Linux/macOS X11:   docker run -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix ...
ENV DISPLAY=host.docker.internal:0.0

ENTRYPOINT ["java", "-jar", "app.jar"]
