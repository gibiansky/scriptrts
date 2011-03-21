package com.scriptrts.net;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.scriptrts.core.Main;
import com.scriptrts.game.Direction;
import com.scriptrts.game.GameObject;
import com.scriptrts.game.Map;
import com.scriptrts.game.Player;
import com.scriptrts.game.SpriteState;
import com.scriptrts.game.TerrainType;


/**
 * Game protocol class to convert objects into primtive types and back
 */
public class GameProtocol {
    /**
     * Write a string to the server.
     * @param out output stream to which to write
     * @param s string to write
     */
    public static void sendString(DataOutputStream out, String s) throws IOException {
        out.writeUTF(s);
    }

    /**
     * Write a color to the server.
     * @param out output stream to which to write
     * @param c color to send
     */
    public static void sendColor(DataOutputStream out, Color c) throws IOException {
        out.writeInt(c.getRed());
        out.writeInt(c.getGreen());
        out.writeInt(c.getBlue());
    }

    /**
     * Write a server request to a server
     * @param out output stream to which to write
     * @param req request to send
     */
    public static void sendRequest(DataOutputStream out, ServerRequest req) throws IOException {
        out.writeInt(req.ordinal());
    }
    /**
     * Write a server response to a client
     * @param out output stream to which to write
     * @param res response to send
     */
    public static void sendResponse(DataOutputStream out, ServerResponse res) throws IOException {
        out.writeInt(res.ordinal());
    }
    /**
     * Write a map to a stream
     * @param out output stream to which to write
     * @param m map to write
     */
    public static void sendMap(DataOutputStream out, Map m) throws IOException {
        int n = m.getN();
        out.writeInt(n);
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                out.writeInt(m.getTileArray()[i][j].ordinal());
    }

    /**
     * Write a direction to a stream
     * @param out output stream to which to write
     * @param d direction to write
     */
    public static void sendDirection(DataOutputStream out, Direction d) throws IOException {
        if(d == null)
            out.writeInt(-1);
        else
            out.writeInt(d.ordinal());
    }

    /**
     * Write a unit state to the server
     * @param out output stream to which to write
     * @param unit unit whose state to send
     */
    public static void sendUnit(DataOutputStream out, GameObject unit) throws IOException {
        Player p = unit.getUnit().getAllegiance();
        int x = unit.getUnit().getX();
        int y = unit.getUnit().getY();
        int state = unit.getState().ordinal();
        int id = unit.getID();
        int speed = unit.getUnit().getSpeed();

        out.writeInt(p.getID());
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(state);
        out.writeInt(id);
        out.writeInt(speed);
        sendDirection(out, unit.getDirection());
        sendDirection(out, unit.getFacingDirection());
        out.flush();
    }

    /**
     * Read a string from the server.
     * @param in input stream from which to read
     * @return read string
     */
    public static String readString(DataInputStream in) throws IOException {
        return in.readUTF();
    }

    /**
     * Write a color to the server.
     * @param in input stream from which to read
     * @return color read
     */
    public static Color readColor(DataInputStream in) throws IOException {
        int red   = in.readInt();
        int green = in.readInt();
        int blue  = in.readInt();
        return new Color(red, green, blue);
    }

    /**
     * Read a request
     * @param in input from which to read
     * @return request from client
     */
    public static ServerRequest readRequest(DataInputStream in) throws IOException {
        return ServerRequest.values()[in.readInt()];
    }
    /**
     * Read a response
     * @param in input from which to read
     * @return response from server
     */
    public static ServerResponse readResponse(DataInputStream in) throws IOException {
        return ServerResponse.values()[in.readInt()];
    }

    /**
     * Read a map from an input 
     * @param in input stream from which to read
     * @return map read from input stream
     */
    public static Map readMap(DataInputStream in) throws IOException {
        int n = in.readInt();
        Map map = new Map(n);
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                map.getTileArray()[i][j] = TerrainType.values()[in.readInt()];
        return map;
    }

    /**
     * Read a direction from the network
     * @param in input stream from which to read
     * @return direction read
     */
    public static Direction readDirection(DataInputStream in) throws IOException {
        int dir = in.readInt();
        if(dir == -1)
            return null;
        else
            return Direction.values()[dir];
    }

    /**
     * Read a unit state from the server into the unit
     * @param in input stream from which to read
     * @return unit with sent state
     */
    public static GameObject readUnit(DataInputStream in) throws IOException {
        int playerID = in.readInt();
        Player p = null;
        for(Player player : Main.getGame().getPlayers())
            if(player.getID() == playerID)
                p = player;

        int x = in.readInt();
        int y = in.readInt();
        int stateOrd = in.readInt();
        int id = in.readInt();
        int speed = in.readInt(); 

        Direction direction = readDirection(in);
        Direction previousDirection = readDirection(in);

        GameObject u = new GameObject();
        u.setParameters(p, x, y, SpriteState.values()[stateOrd], id, speed, direction, previousDirection);
        return u;
    }

}
