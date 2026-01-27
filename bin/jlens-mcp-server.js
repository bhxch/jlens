#!/usr/bin/env node

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');
const os = require('os');

const JAR_NAME_PREFIX = 'jlens-mcp-server-';

async function main() {
    // 1. Determine Java Path
    // Priority: --java-path arg > JLENS_JAVA_HOME env > JAVA_HOME env > 'java' in PATH
    let javaPath = 'java';
    const envJavaHome = process.env.JLENS_JAVA_HOME || process.env.JAVA_HOME;
    
    // Parse arguments
    const args = process.argv.slice(2);
    const mcpArgs = [];
    let jarPathArg = null;

    for (let i = 0; i < args.length; i++) {
        if (args[i] === '--java-path' && i + 1 < args.length) {
            javaPath = args[++i];
        } else if (args[i] === '--jar-path' && i + 1 < args.length) {
            jarPathArg = args[++i];
        } else {
            mcpArgs.push(args[i]);
        }
    }

    if (javaPath === 'java' && envJavaHome) {
        const platformJava = process.platform === 'win32' ? 'java.exe' : 'java';
        const candidate = path.join(envJavaHome, 'bin', platformJava);
        if (fs.existsSync(candidate)) {
            javaPath = candidate;
        }
    }

    // 2. Determine JAR Path
    let jarPath = jarPathArg;
    if (!jarPath) {
        jarPath = findLocalJar();
    }

    if (!jarPath) {
        console.error(`Error: Could not find jlens-mcp-server JAR file.`);
        console.error(`Checked: ${path.join(__dirname, JAR_NAME_PREFIX + '*.jar')}`);
        process.exit(1);
    }

    // 3. Construct Arguments
    // Priority: Env Vars > CLI Args (CLI args appended after Env args)
    let envArgs = [];
    if (process.env.JLENS_ARGS) {
        // Simple splitting, preserving quoted strings would require more complex parsing
        // but for now we assume simple space-separated args
        envArgs = process.env.JLENS_ARGS.split(/\s+/).filter(s => s.length > 0);
    }

    // Check for Java
    const javaCheck = spawn(javaPath, ['-version']);
    javaCheck.on('error', (err) => {
        console.error(`Error: Java not found at "${javaPath}". Please ensure Java 25+ is installed.`);
        process.exit(1);
    });

    javaCheck.on('exit', (code) => {
        // Execute the JAR
        const finalArgs = [...envArgs, '-jar', jarPath, ...mcpArgs];
        const child = spawn(javaPath, finalArgs, { stdio: 'inherit' });
        
        child.on('exit', (exitCode) => {
            process.exit(exitCode);
        });
    });
}

function findLocalJar() {
    // 1. Look in the same directory as this script (Production/Distribution)
    const binDir = __dirname;
    let jar = findJarInDir(binDir);
    if (jar) return jar;

    // 2. Look in ../target (Development)
    const targetDir = path.join(__dirname, '..', 'target');
    if (fs.existsSync(targetDir)) {
        jar = findJarInDir(targetDir);
        if (jar) return jar;
    }

    return null;
}

function findJarInDir(dir) {
    if (!fs.existsSync(dir)) return null;
    const files = fs.readdirSync(dir);
    // Find jlens-mcp-server-*.jar but NOT original-*.jar and NOT sources/javadoc
    const jarFile = files.find(f => 
        f.startsWith(JAR_NAME_PREFIX) && 
        f.endsWith('.jar') && 
        !f.includes('original') &&
        !f.includes('sources') &&
        !f.includes('javadoc')
    );
    return jarFile ? path.join(dir, jarFile) : null;
}

main();