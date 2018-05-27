package actorgameoflife.gameoflifegui.mainpanel;

import actorgameoflife.gameoflifegui.matrixtoimage.ConvertToImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class MainPanel extends JFrame implements GameOfLifeGUI {

    private static final String LIVING_TEXT = "Living cell: ";
    private final Set<MainPanelObserver> guiObserver = new HashSet<>();

    private ScrollingBoard boardPanel;
    private JLabel aliveCellLabel = new JLabel(LIVING_TEXT + "0");

    public MainPanel(int boardRow, int boardColumn, int visualizedRow, int visualizedColumn) {
        this.setTitle("The Game of Life");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        boardPanel = new ScrollingBoard(boardRow, boardColumn, visualizedRow, visualizedColumn);
        this.getContentPane().add(boardPanel, BorderLayout.CENTER);

        JPanel commandPanel = new JPanel();
        JButton start = new JButton("Start");
        start.addActionListener(e -> notifyStart());
        commandPanel.add(start);
        JButton stop = new JButton("Stop");
        stop.addActionListener(e -> notifyStop());
        commandPanel.add(stop);
        commandPanel.add(aliveCellLabel);
        this.getContentPane().add(commandPanel,BorderLayout.PAGE_END);
        this.setSize(1000, 1000);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @Override
    public void addObserver(MainPanelObserver observer){
        this.guiObserver.add(observer);
    }

    @Override
    public void updateBoard(BufferedImage boardImage) {
        boardPanel.updateDisplayedBoard(boardImage);
        //notifyUpdated();
    }

    @Override
    public void updateLivingCellLabel(int livingCell) {
        aliveCellLabel.setText(LIVING_TEXT + livingCell);
    }

    private void notifyStart(){
        for (final MainPanelObserver observer : this.guiObserver){
            observer.startEvent();
        }
    }
    private void notifyStop(){
        for (final MainPanelObserver observer : this.guiObserver){
            observer.stopEvent();
        }
    }

    private void notifyUpdated(){
        for (final MainPanelObserver observer : this.guiObserver){
            observer.boardUpdated();
        }
    }

    private void notifyScroll(int x, int y){
        for (final MainPanelObserver observer : this.guiObserver){
            observer.scrollEvent(x, y);
        }
    }









    public class ScrollingBoard extends JPanel{

        private static final int PADDING = 10;
        private boolean initialized = false;

        private JLabel boardDisplay = new JLabel();
        private JScrollBar horizontalScroller;
        private JScrollBar verticalScroller;
        private int scrollX = 0;
        private int scrollY = 0;
        private int canvasWidth, canvasHeight;

        private BufferedImage board;

        public ScrollingBoard(int boardRow, int boardColumn, int visualizedRow, int visualizedColumn) {
            horizontalScroller = new JScrollBar(JScrollBar.HORIZONTAL);
            horizontalScroller.addAdjustmentListener((e)-> {
                scrollX = horizontalScroller.getValue();
                notifyScroll(scrollX, scrollY);
            });
            horizontalScroller.setMinimum (0);
            verticalScroller = new JScrollBar(JScrollBar.VERTICAL);
            verticalScroller.addAdjustmentListener((e)-> {
                scrollY = verticalScroller.getValue();
                notifyScroll(scrollX, scrollY);
            });
            verticalScroller.setMinimum (0);
            this.setLayout(new BorderLayout());
            this.add(boardDisplay,BorderLayout.CENTER);
            this.add(horizontalScroller,BorderLayout.PAGE_END);
            this.add(verticalScroller,BorderLayout.LINE_END);

            if(boardRow < visualizedRow) {
                visualizedRow = boardRow;
            }
            if(boardColumn < visualizedColumn) {
                visualizedColumn = boardColumn;
            }
            verticalScroller.setMaximum(boardRow - visualizedRow + PADDING);
            horizontalScroller.setMaximum(boardColumn - visualizedColumn + PADDING);

            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    super.componentResized(e);
                    canvasWidth = e.getComponent().getWidth() - verticalScroller.getWidth();
                    canvasHeight = e.getComponent().getHeight() - horizontalScroller.getHeight();
                    if(board != null){
                        updateImage();
                    }
                }
            });
        }

        public void updateDisplayedBoard(BufferedImage boardImage){
            this.board = boardImage;
            if(!initialized){
                initialized = true;
                canvasWidth = this.getWidth() - verticalScroller.getWidth();
                canvasHeight = this.getHeight() - horizontalScroller.getHeight();
            }
            updateImage();
        }

        private void updateImage(){
            if(board != null && canvasWidth > 0 && canvasHeight > 0){
                boardDisplay.setIcon(new ImageIcon(ConvertToImage.resize(board, canvasWidth,canvasHeight)));
            }

        }

    }




}
