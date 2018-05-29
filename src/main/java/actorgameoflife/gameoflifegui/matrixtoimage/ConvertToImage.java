package actorgameoflife.gameoflifegui.matrixtoimage;

import actorgameoflife.board.Board;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

public class ConvertToImage {

    private final static byte BLACK = (byte)0, WHITE = (byte)255;
    private final static byte[] map = {BLACK, WHITE};
    private final static IndexColorModel icm = new IndexColorModel(1, map.length, map, map, map);

    public static BufferedImage boardToImage(Board board){
        return boardToImage(board,0,0, board.getRow(), board.getColumn());
    }

    public static BufferedImage boardToImage(Board board, int startRow, int startColumn, int finalRow, int finalColumn){
        int[] data = new int[finalRow*finalColumn];
        for(int row = 0; row < finalRow; row++)
            for(int column = 0; column < finalColumn; column++)
                data[row*finalColumn + column] = board.isCellAlive(startRow + row,startColumn + column) ? BLACK : WHITE;

        WritableRaster raster = icm.createCompatibleWritableRaster(finalRow, finalColumn);
        raster.setPixels(0, 0, finalRow, finalColumn, data);
        return new BufferedImage(icm, raster, false, null);

    }

    public static BufferedImage resize(BufferedImage image, int newWidth, int newHeight) {
        Image tmp = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }

}
