import subprocess
import sys
import os
import glob
from pathlib import Path

# Version is illustrative here; we mostly rely on finding the JAR matching the prefix
JAR_NAME_PREFIX = "jlens-mcp-server-"

def find_local_jar():
    package_dir = Path(__file__).parent
    
    # 1. Look in the package directory (Production/Installed)
    matches = glob.glob(str(package_dir / f"{JAR_NAME_PREFIX}*.jar"))
    matches = [m for m in matches if "original" not in m and "sources" not in m and "javadoc" not in m]
    if matches:
        return matches[0]

    # 2. Look in ../target (Development)
    root_dir = package_dir.parent
    target_dir = root_dir / "target"
    if target_dir.exists():
        matches = glob.glob(str(target_dir / f"{JAR_NAME_PREFIX}*.jar"))
        matches = [m for m in matches if "original" not in m and "sources" not in m and "javadoc" not in m]
        if matches:
            return matches[0]
            
    return None

def main():
    # 1. Determine Java Path
    # Priority: --java-path arg > JLENS_JAVA_HOME env > JAVA_HOME env > 'java' in PATH
    java_path = "java"
    env_java_home = os.environ.get("JLENS_JAVA_HOME") or os.environ.get("JAVA_HOME")
    
    args = sys.argv[1:]
    mcp_args = []
    jar_path_arg = None

    i = 0
    while i < len(args):
        if args[i] == "--java-path" and i + 1 < len(args):
            java_path = args[i+1]
            i += 2
        elif args[i] == "--jar-path" and i + 1 < len(args):
            jar_path_arg = args[i+1]
            i += 2
        else:
            mcp_args.append(args[i])
            i += 1

    if java_path == "java" and env_java_home:
        # Try to find java in JAVA_HOME/bin
        bin_name = "java.exe" if sys.platform == "win32" else "java"
        candidate = Path(env_java_home) / "bin" / bin_name
        if candidate.exists():
            java_path = str(candidate)

    # 2. Determine JAR Path
    jar_path = jar_path_arg
    if not jar_path:
        jar_path = find_local_jar()

    if not jar_path:
        print("Error: Could not find jlens-mcp-server JAR file.", file=sys.stderr)
        package_dir = Path(__file__).parent
        print(f"Checked: {package_dir / f'{JAR_NAME_PREFIX}*.jar'}", file=sys.stderr)
        sys.exit(1)

    # 3. Construct Arguments
    env_args = []
    jlens_env_args = os.environ.get("JLENS_ARGS")
    if jlens_env_args:
        # Simple split
        env_args = jlens_env_args.split()

    # Check Java
    try:
        subprocess.run([java_path, "-version"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
    except (subprocess.CalledProcessError, FileNotFoundError):
        print(f"Error: Java not found at '{java_path}'. Please ensure Java 25+ is installed.", file=sys.stderr)
        sys.exit(1)

    # Execute
    cmd = [java_path] + env_args + ["-jar", jar_path] + mcp_args
    try:
        # Replace current process or just run it
        # Popen allows capturing signals better in some contexts, but run is simpler.
        # However, for an MCP server, we want to forward stdin/stdout perfectly.
        process = subprocess.Popen(cmd)
        process.wait()
        sys.exit(process.returncode)
    except KeyboardInterrupt:
        process.terminate()
        sys.exit(0)

if __name__ == "__main__":
    main()