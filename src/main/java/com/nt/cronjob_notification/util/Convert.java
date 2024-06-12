package com.nt.cronjob_notification.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.SQLException;

public class Convert {
    public static String clobToString(Clob clob) throws IOException, SQLException {
        try (Reader reader = clob.getCharacterStream();
             StringWriter writer = new StringWriter()) {
            
            char[] buffer = new char[4096]; // Define an appropriate buffer size
            int charsRead;
            
            while ((charsRead = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, charsRead);
            }
            
            return writer.toString();
        }
    }
}
