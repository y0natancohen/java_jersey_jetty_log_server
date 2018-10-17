package com.yonatan.res;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringJoiner;


@Path("logs")
public class MyServer {

    private Connection connection = null;
    private Statement statement = null;
    private ResultSet resultSet = null;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getReadableMessage() throws Exception {

        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM logdb.logs2");
            String result = getResultsText(resultSet);
            System.out.println(result);
            return "RECORDED LOGS:\n\n" + result;

        } finally {
            close();
        }
    }


    @Path("/{text}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String saveLog(@PathParam("text") String txt) throws Exception {
        try {
            connection = getConnection();
            Date date = new Date();
            String currentTime = formatter.format(date);
            PreparedStatement preparedStatement = connection
                    .prepareStatement("INSERT INTO logdb.logs2 (Text, DateTime) " +
                            "VALUES (\"" + txt + "\", \"" + currentTime + "\")");
            preparedStatement.executeUpdate();

            return "log:\n\""+ txt + "\"\nwas created at " + currentTime + " successfully.";

        } finally {
            close();
        }
    }

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager
                .getConnection("jdbc:mysql://localhost/logdb?"
                        + "user=sqluser&password=sqlpass");
    }

    private String getResultsText(ResultSet resultSet) throws SQLException {
        StringJoiner sj = new StringJoiner("\n\n\n");
        while (resultSet.next()) {
            String line = "Recorded at: " +
                    resultSet.getTimestamp(3).toString() + "\n" +
                    resultSet.getString("text");
            sj.add(line);
        }
        return sj.toString();
    }

    private void close() throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }

        if (statement != null) {
            statement.close();
        }

        if (connection != null) {
            connection.close();
        }
    }
}