
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.*;

public class FileDropWindow {

    public static void main(String[] args) {
        new FileDropWindow();
    }

    public FileDropWindow() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                JFrame frame = new JFrame("Schedule parser");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                //frame.setSize(500, 400);
                frame.add(new DropPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class DropPane extends JPanel {

        private DropTarget dropTarget;
        private DropTargetHandler dropTargetHandler;
        private Point dragPoint;

        private boolean dragOver = false;
        private BufferedImage target;

        private JLabel message;

        public DropPane() {

            setLayout(new GridBagLayout());
            setSize(500, 200);
            //JTextArea jta = new JTextArea(500, 400);
            //add(jta);
            
            //jta.setText("Dra hit och släpp din schema-fil från IKEA. \n Programmet kommer att generera en csv-fil som google calendar kan importera. ");
            message = new JLabel();
            message.setPreferredSize(new Dimension(500, 450));
            message.setFont(message.getFont().deriveFont(Font.BOLD, 24));
            add(message);
            
            message.setText("<html><div style='text-align: center;'>Dra hit och släpp din schema-fil från IKEA. <br><br>   Programmet kommer att generera en csv-fil som google calendar kan importera.</html>");

        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(500, 450);
        }

        protected DropTarget getMyDropTarget() {
            if (dropTarget == null) {
                dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null);
            }
            return dropTarget;
        }

        protected DropTargetHandler getDropTargetHandler() {
            if (dropTargetHandler == null) {
                dropTargetHandler = new DropTargetHandler();
            }
            return dropTargetHandler;
        }

        @Override
        public void addNotify() {
            super.addNotify();
            try {
                getMyDropTarget().addDropTargetListener(getDropTargetHandler());
            } catch (TooManyListenersException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            getMyDropTarget().removeDropTargetListener(getDropTargetHandler());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (dragOver) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 255, 0, 64));
                g2d.fill(new Rectangle(getWidth(), getHeight()));
                if (dragPoint != null && target != null) {
                    int x = dragPoint.x - 12;
                    int y = dragPoint.y - 12;
                    g2d.drawImage(target, x, y, this);
                }
                g2d.dispose();
            }
        }

        protected void handleFiles(final List files) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    //message.setText("You dropped " + files.size() + " files");
                	String filePath = files.get(0).toString();
                    message.setText("Jobbar...");
                    ScheduleParser scheduleParser = new ScheduleParser();
                    int result = scheduleParser.parseSchedule(filePath);
                    evaluateResult(result);
                    
                }
            };
            SwingUtilities.invokeLater(run);
        }
        
        private void evaluateResult(int result) {
        	if (result == 0) {
        		message.setText("<html><div style='text-align: center;'>"
                		+ "Schema-filen är skapad. <br>"
                		+ "Du kan importera den i google calender <br>"
                		+ "genom följande steg: </div>"
                		+ "1. Gå in under 'inställningar' (litet<br>"
                		+ "kugghjul uppe till höger)<br>"
                		+ "2. Öppna fliken 'kalendrar'<br>"
                		+ "3. Klicka på 'importera kalendrar' mellan  <br>"
                		+ "avsnitten Mina kalendrar och Andra kalendrar. <br>"
                		+ "4. Välj filen, den heter som pdfen fast med <br>"
                		+ "tillägget _Google-calendar och ligger i<br>"
                		+ "samma mapp som pdfen.<br>"
                		+ "5. Välj rätt kalender och klicka på importera.<br>"
                		+ "6. Klar!"
                		+ "</html>");
        	} else if (result == 1) {
        		message.setText("<html><div style='text-align: center;'>"
        				+ "Filen var inte en pdf-fil. <br>"
        				+ "Kolla så att det är rätt fil, eller om den har<br>"
        				+ "fel filändelse.</html>");
        	} else if (result == 2) {
        		message.setText("<html><div style='text-align: center;'>"
        				+ "Det gick inte att hämta ut schema-event från filen. <br>"
        				+ "Dubbelkolla att det är rätt fil, annars så kan det <br>"
        				+ "vara så att filen använder ett annat format än vad <br>"
        				+ "programmet är gjort för.</html>");
        	} else if (result == 3) {
        		message.setText("<html><div style='text-align: center;'>"
        				+ "Det gick inte att skriva schema-filen.<br>"
        				+ "Kolla skrivrättigheterna i mappen eller<br>"
        				+ "testa igen, eller med en annan mapp,<br>"
        				+ "kanske skrivbordet.</html>");
        	} else {
        		message.setText("<html><div style='text-align: center;'>"
        				+ "Ett oväntat fel. <br>");
        	}
        }

        protected class DropTargetHandler implements DropTargetListener {

            protected void processDrag(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                processDrag(dtde);
                //SwingUtilities.invokeLater(new DragUpdate(true, dtde.getLocation()));
                repaint();
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                processDrag(dtde);
                //SwingUtilities.invokeLater(new DragUpdate(true, dtde.getLocation()));
                repaint();
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                //SwingUtilities.invokeLater(new DragUpdate(false, null));
                repaint();
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {

                //SwingUtilities.invokeLater(new DragUpdate(false, null));

                Transferable transferable = dtde.getTransferable();
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrop(dtde.getDropAction());
                    try {
                    	String data = transferable.getTransferData(DataFlavor.javaFileListFlavor).toString();
                        List transferData = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        if (transferData != null && transferData.size() > 0) {

                        	//System.out.println("Data: " + data);
                            handleFiles(transferData);
                            dtde.dropComplete(true);
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    dtde.rejectDrop();
                }
            }
        }

    }
}