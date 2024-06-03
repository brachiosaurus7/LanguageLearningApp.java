public class Main {

    private static String url;
    private static String username;
    private static String password;
    private static String modifiedUrl; 

    public static void main(String[] args) {
        BuildConnection buildConnection = new BuildConnection();

        while (buildConnection.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        url = buildConnection.getUrl();
        username = buildConnection.getUsername();
        password = buildConnection.getPassword();

        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        modifiedUrl = modifyUrl(url);
        System.out.println("Modified URL: " + modifiedUrl);

        Thread openloginwindow = new Thread(() -> {
            LoginWindow.main(new String[]{});
        });
        openloginwindow.start();

        try {
            openloginwindow.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static String modifyUrl(String inputUrl) {
        String modifiedUrl = inputUrl.replace("xepdb1", "XE");
        return modifiedUrl;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getModifiedUrl() {
        return modifiedUrl;
    }
}
