package Test;

import common.java.httpServer.booter;
import common.java.nlogger.nlogger;

public class TestMenu {
    public static void main(String[] args) {
        booter booter = new booter();
        try {
            System.out.println("GrapeMenu");
            System.setProperty("AppName", "GrapeMenu");
            booter.start(1006);
        } catch (Exception e) {
            nlogger.logout(e);
        } 
    }
}
