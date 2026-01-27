#!/usr/bin/env node

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');
const os = require('os');
const https = require('https');

const VERSION = '1.1.1';
const REPO = 'bhxch/jlens';
const JAR_NAME_PREFIX = 'jlens-mcp-server-';
const CACHE_DIR = path.join(os.homedir(), '.jlens', 'cache');

async function main() {
    const args = process.argv.slice(2);
    
    // Parse arguments
    let javaPath = 'java';
    let jarPath = '';
    const mcpArgs = [];

    for (let i = 0; i < args.length; i++) {
        if (args[i] === '--java-path' && i + 1 < args.length) {
            javaPath = args[++i];
        } else if (args[i] === '--jar-path' && i + 1 < args.length) {
            jarPath = args[++i];
        } else {
            mcpArgs.push(args[i]);
        }
    }

    // 1. Try to find JAR locally if not specified
    if (!jarPath) {
        jarPath = findLocalJar();
    }

    // 2. If still not found, check cache
    if (!jarPath) {
        const cachedJar = path.join(CACHE_DIR, `${JAR_NAME_PREFIX}${VERSION}.jar`);
        if (fs.existsSync(cachedJar)) {
            jarPath = cachedJar;
        }
    }

    // 3. If still not found, download it
    if (!jarPath) {
        console.error(`JAR not found locally. Attempting to download version ${VERSION}...`);
        try {
            jarPath = await downloadJar(VERSION);
        } catch (err) {
            console.error(`Error downloading JAR: ${err.message}`);
            process.exit(1);
        }
    }

    // Check for Java
    const javaCheck = spawn(javaPath, ['-version']);
    javaCheck.on('error', (err) => {
        console.error(`Error: Java not found at "${javaPath}". Please ensure Java 25+ is installed and in PATH, or specify via --java-path.`);
        process.exit(1);
    });

    javaCheck.on('exit', (code) => {
        // Execute the JAR
        const finalArgs = ['-jar', jarPath, ...mcpArgs];
        const child = spawn(javaPath, finalArgs, { stdio: 'inherit' });
        
        child.on('exit', (exitCode) => {
            process.exit(exitCode);
        });
    });
}

function findLocalJar() {
    const rootDir = path.join(__dirname, '..');
    const targetDir = path.join(rootDir, 'target');
    
    const searchPaths = [targetDir, rootDir];
    for (const dir of searchPaths) {
        if (fs.existsSync(dir)) {
            const files = fs.readdirSync(dir);
            const jarFile = files.find(f => f.startsWith(JAR_NAME_PREFIX) && f.endsWith('.jar') && !f.includes('original'));
            if (jarFile) return path.join(dir, jarFile);
        }
    }
    return null;
}

async function downloadJar(version) {
    if (!fs.existsSync(CACHE_DIR)) {
        fs.mkdirSync(CACHE_DIR, { recursive: true });
    }

    const dest = path.join(CACHE_DIR, `${JAR_NAME_PREFIX}${version}.jar`);
    const url = `https://github.com/${REPO}/releases/download/v${version}/${JAR_NAME_PREFIX}${version}.jar`;

    return new Promise((resolve, reject) => {
        const file = fs.createWriteStream(dest);
        
        function get(downloadUrl) {
            https.get(downloadUrl, (response) => {
                if (response.statusCode === 302 || response.statusCode === 301) {
                    get(response.headers.location);
                    return;
                }
                
                if (response.statusCode !== 200) {
                    reject(new Error(`Failed to download JAR: HTTP ${response.statusCode}`));
                    return;
                }

                response.pipe(file);
                file.on('finish', () => {
                    file.close();
                    resolve(dest);
                });
            }).on('error', (err) => {
                fs.unlink(dest, () => reject(err));
            });
        }
        
        get(url);
    });
}

main();
