
import java.io.*;
import java.nio.file.*;


public class GitAuto {

    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("Usage:");
            System.out.println("java GitAutomation <repo-url> <operation> <file-name> <file-content> <working-dir>");
            System.out.println("operation = add | update");
            return;
        }

        String repoUrl = args[0];
        String operation = args[1]; // "add" or "update"
        String fileName = args[2];
        String fileContent = args[3];
        String workingDir = args[4];

        try {
            // ✅ Ensure the working directory exists
            File workingDirFile = new File(workingDir);
            if (!workingDirFile.exists()) {
                boolean created = workingDirFile.mkdirs();
                if (!created) {
                    throw new IOException("Failed to create working directory: " + workingDir);
                }
            }

            // ✅ Get repo folder name from the URL
            String repoName = getRepoNameFromUrl(repoUrl);
            File repoDir = new File(workingDirFile, repoName);

            // ✅ Clone the repo only if not already cloned
            if (!repoDir.exists()) {
                runCommand(workingDir, "git", "clone", repoUrl);
            }

            File targetFile = new File(repoDir, fileName);

            // ✅ Perform the requested operation
            if (operation.equalsIgnoreCase("add")) {
                writeNewFile(targetFile, fileContent);
            } else if (operation.equalsIgnoreCase("update")) {
                appendToFile(targetFile, fileContent);
            } else {
                throw new IllegalArgumentException("Invalid operation. Use 'add' or 'update'.");
            }

            // ✅ Stage, commit, and push the change
            runCommand(repoDir.getAbsolutePath(), "git", "add", fileName);
            runCommand(repoDir.getAbsolutePath(), "git", "commit", "-m", "Automated " + operation + " via Java CLI");
            runCommand(repoDir.getAbsolutePath(), "git", "push");

            System.out.println("✅ Operation completed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔧 Utility to run a shell command in the specified directory
    private static void runCommand(String workingDir, String... command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(workingDir));
        builder.redirectErrorStream(true);
        Process process = builder.start();

        // Print output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed: " + String.join(" ", command));
        }
    }

    // 🔧 Extracts repo name from URL
    private static String getRepoNameFromUrl(String url) {
        String[] parts = url.split("/");
        String repo = parts[parts.length - 1];
        return repo.endsWith(".git") ? repo.substring(0, repo.length() - 4) : repo;
    }

    // ✅ Create a new file (fails if it exists)
    private static void writeNewFile(File file, String content) throws IOException {
        if (file.exists()) {
            throw new IOException("File already exists: " + file.getAbsolutePath());
        }
        Files.writeString(file.toPath(), content, StandardOpenOption.CREATE_NEW);
    }

    // ✅ Append to an existing file (fails if it doesn’t exist)
    private static void appendToFile(File file, String content) throws IOException {
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }
        Files.writeString(file.toPath(), content, StandardOpenOption.APPEND);
    }
}

//javac GitAuto

//java GitAuto https://github.com/AdnanVanni/gitjava add hello.txt "This is a new file created via Java CLI." C:\Test

//java GitAuto https://github.com/AdnanVanni/gitjava update README.md "Appended this line via Java CLI." C:\Test
 
