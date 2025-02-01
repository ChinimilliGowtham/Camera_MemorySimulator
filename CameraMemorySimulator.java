import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class MemoryStorageBlock<T> {
    private int startAddress;
    private int size;
    private boolean allocated;
    private T mediaObject;

    public MemoryStorageBlock(int startAddress, int size) {
        this.startAddress = startAddress;
        this.size = size;
        this.allocated = false;
        this.mediaObject = null;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isAllocated() {
        return allocated;
    }

    public void allocate() {
        allocated = true;
    }

    public void deallocate() {
        allocated = false;
    }

    public T getMediaObject() {
        return mediaObject;
    }

    public void setMediaObject(T mediaObject) {
        this.mediaObject = mediaObject;
    }
}

class Memory {
    private int totalSize;
    private List<MemoryStorageBlock> blocks;

    public Memory(int totalSize) {
        this.totalSize = totalSize;
        this.blocks = new ArrayList<>();
        blocks.add(new MemoryStorageBlock(0, totalSize));
    }

    public boolean allocateFirstFit(Object mediaObject) {
        return allocateMedia(mediaObject, AllocationStrategy.FIRST_FIT);
    }

    public boolean allocateBestFit(Object mediaObject) {
        return allocateMedia(mediaObject, AllocationStrategy.BEST_FIT);
    }

    public boolean allocateWorstFit(Object mediaObject) {
        return allocateMedia(mediaObject, AllocationStrategy.WORST_FIT);
    }

    private enum AllocationStrategy {
        FIRST_FIT, BEST_FIT, WORST_FIT
    }

    private MemoryStorageBlock findAppropriateBlock(int mediaSize, AllocationStrategy strategy) {
        MemoryStorageBlock selectedBlock = null;

        for (MemoryStorageBlock block : blocks) {
            if (!block.isAllocated() && block.getSize() >= mediaSize) {
                if (selectedBlock == null) {
                    selectedBlock = block;
                } else {
                    switch (strategy) {
                        case BEST_FIT:
                            if (block.getSize() < selectedBlock.getSize()) {
                                selectedBlock = block;
                            }
                            break;
                        case WORST_FIT:
                            if (block.getSize() > selectedBlock.getSize()) {
                                selectedBlock = block;
                            }
                            break;
                        // For FIRST_FIT, the first suitable block found is used
                        default:
                            break;
                    }
                }
            }
        }
        return selectedBlock;
    }

    private boolean allocateMedia(Object mediaObject, AllocationStrategy strategy) {
        int mediaSize = calculateMediaSize(mediaObject);

        MemoryStorageBlock blockToAllocate = findAppropriateBlock(mediaSize, strategy);

        if (blockToAllocate != null) {
            if (blockToAllocate.getSize() > mediaSize) {
                MemoryStorageBlock newBlock = new MemoryStorageBlock(blockToAllocate.getStartAddress() + mediaSize, blockToAllocate.getSize() - mediaSize);
                blocks.add(blocks.indexOf(blockToAllocate) + 1, newBlock);
            }

            blockToAllocate.allocate();
            blockToAllocate.setMediaObject(mediaObject);
            return true;
        }

        return false;
    }

    public void deallocate(int startAddress) {
        for (MemoryStorageBlock block : blocks) {
            if (block.getStartAddress() == startAddress && block.isAllocated()) {
                block.deallocate();
                mergeFreeBlocks();
                return;
            }
        }
    }

    public void clearMemory() {
        blocks.clear();
        blocks.add(new MemoryStorageBlock(0, totalSize));
    }

    private void mergeFreeBlocks() {
        List<MemoryStorageBlock> mergedBlocks = new ArrayList<>();
        MemoryStorageBlock currentBlock = blocks.get(0);

        for (int i = 1; i < blocks.size(); i++) {
            MemoryStorageBlock nextBlock = blocks.get(i);

            if (!currentBlock.isAllocated() && !nextBlock.isAllocated()) {
                currentBlock.setSize(currentBlock.getSize() + nextBlock.getSize());
                blocks.remove(i);
                i--;  // Adjust the index to recheck the merged block with the previous one.
            } else {
                mergedBlocks.add(currentBlock);
                currentBlock = nextBlock;
            }
        }

        mergedBlocks.add(currentBlock);
        blocks = mergedBlocks;
    }

    private int calculateMediaSize(Object mediaObject) {
        if (mediaObject instanceof Image) {
            return ((Image) mediaObject).getWidth() * ((Image) mediaObject).getHeight();
        } else if (mediaObject instanceof Video) {
            return ((Video) mediaObject).getDuration() * 10;  // Adjust the factor based on your requirements
        }
        return 0;
    }

    public List<MemoryStorageBlock> getBlocks() {
        return blocks;
    }

    public int getTotalSize() {
        return totalSize;
    }
}

class Process {
    private int size;

    public Process(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}

class Image {
    private int width;
    private int height;

    public Image(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

class Video {
    private int duration;

    public Video(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }
}

public class CameraMemorySimulator {
    private Memory memory;
    private JFrame frame;
    private JTextArea memoryStatus;
    private JRadioButton firstFitRadio;
    private JRadioButton bestFitRadio;
    private JRadioButton worstFitRadio;

    public CameraMemorySimulator(int totalMemorySize) {
        memory = new Memory(totalMemorySize);
        frame = new JFrame("Memory Allocation Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        memoryStatus = new JTextArea(10, 40);
        firstFitRadio = new JRadioButton("First Fit", true);
        bestFitRadio = new JRadioButton("Best Fit");
        worstFitRadio = new JRadioButton("Worst Fit");
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(firstFitRadio);
        radioGroup.add(bestFitRadio);
        radioGroup.add(worstFitRadio);

        firstFitRadio.addActionListener(e -> updateMemoryDisplay());
        bestFitRadio.addActionListener(e -> updateMemoryDisplay());
        worstFitRadio.addActionListener(e -> updateMemoryDisplay());

        JButton allocateButton = new JButton("Allocate Memory");
        JButton deallocateButton = new JButton("Deallocate Memory");
        JButton clearMemoryButton = new JButton("Clear Memory");

        allocateButton.addActionListener(e -> {
            if (firstFitRadio.isSelected() || bestFitRadio.isSelected() || worstFitRadio.isSelected()) {
                handleMediaAllocation();
            } else {
                memoryStatus.append("Select a memory allocation strategy.\n");
            }
            updateMemoryDisplay();
        });

        deallocateButton.addActionListener(e -> {
            int startAddress = Integer.parseInt(JOptionPane.showInputDialog("Enter start address to deallocate:"));
            memory.deallocate(startAddress);
            memoryStatus.append("Memory deallocated successfully.\n");
            updateMemoryDisplay();
        });

        clearMemoryButton.addActionListener(e -> {
            memory.clearMemory();
            memoryStatus.setText("Memory cleared.\n");
            updateMemoryDisplay();
        });

        frame.getContentPane().setLayout(new FlowLayout());
        frame.add(memoryStatus);
        frame.add(firstFitRadio);
        frame.add(bestFitRadio);
        frame.add(worstFitRadio);
        frame.add(allocateButton);
        frame.add(deallocateButton);
        frame.add(clearMemoryButton);

        frame.pack();
        frame.setVisible(true);
        updateMemoryDisplay();
    }

    private void handleMediaAllocation() {
        int width, height, duration;
        boolean isImage;
    
        int mediaTypeChoice = JOptionPane.showOptionDialog(
                frame,
                "Select the media type:",
                "Media Type",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Image", "Video"},
                "Image");
    
        if (mediaTypeChoice == JOptionPane.YES_OPTION) {
            isImage = true;
            width = Integer.parseInt(JOptionPane.showInputDialog("Enter width for the image:"));
            height = Integer.parseInt(JOptionPane.showInputDialog("Enter height for the image:"));
    
            // Check if the requested image size exceeds the available memory
            if (width * height > memory.getTotalSize()) {
                JOptionPane.showMessageDialog(frame, "Image size exceeds available memory. Please choose a smaller size.");
                return;
            }
    
            duration = 0;  // Set duration to 0 for images
        } else {
            isImage = false;
            width = 0;  // Set width and height to 0 for videos
            height = 0;
            duration = Integer.parseInt(JOptionPane.showInputDialog("Enter duration for the video:"));
        }
    
        if (firstFitRadio.isSelected()) {
            memory.allocateFirstFit(isImage ? new Image(width, height) : new Video(duration));
        } else if (bestFitRadio.isSelected()) {
            memory.allocateBestFit(isImage ? new Image(width, height) : new Video(duration));
        } else if (worstFitRadio.isSelected()) {
            memory.allocateWorstFit(isImage ? new Image(width, height) : new Video(duration));
        }
    }
    

    private void updateMemoryDisplay() {
        memoryStatus.setText("");
        if (firstFitRadio.isSelected()) {
            memoryStatus.append("Selected Memory Allocation Strategy: First Fit\n\n");
        } else if (bestFitRadio.isSelected()) {
            memoryStatus.append("Selected Memory Allocation Strategy: Best Fit\n\n");
        } else if (worstFitRadio.isSelected()) {
            memoryStatus.append("Selected Memory Allocation Strategy: Worst Fit\n\n");
        }

        for (MemoryStorageBlock block : memory.getBlocks()) {
            String blockStatus = block.isAllocated() ? "Allocated" : "Free";
            memoryStatus.append(String.format("Block %d-%d: %s\n", block.getStartAddress(), block.getStartAddress() + block.getSize(), blockStatus));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CameraMemorySimulator(1024);
        });
    }
}
