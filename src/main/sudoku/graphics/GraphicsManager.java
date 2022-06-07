package main.sudoku.graphics;

import main.FileLoader;
import main.sudoku.UniqueSet;

public class GraphicsManager {
    private static GraphicsManager instance;
    private Class[] constraintsClasses, constraintGraphicsClasses, graphicsClasses;
    private GraphicInfo[] graphicInfo, constraintInfo;

    public GraphicsManager() {
        constraintsClasses = new Class[]{
                UniqueSet.class
        };
        constraintGraphicsClasses = new Class[]{
                UniqueLine.class, UniqueRect.class
        };
        graphicsClasses = new Class[]{
                Line.class, Rect.class, CellClues.class
        };
        graphicInfo = new GraphicInfo[graphicsClasses.length];
        FileLoader loader = FileLoader.getInstance();
        String text = loader.LoadFile("graphics_info.txt");
        String[] data = text.split("\n<split>\n");
        for (int i = 0; i < graphicInfo.length; i++) {
            graphicInfo[i] = new GraphicInfo();
            graphicInfo[i].name = data[i * 3];
            graphicInfo[i].description = data[i * 3 + 1];
            graphicInfo[i].image = loader.ReadImage(data[i * 3 + 2]);
        }

        constraintInfo = new GraphicInfo[constraintGraphicsClasses.length];
        text = loader.LoadFile("constraints_info.txt");
        System.out.println(text);
        data = text.split("\n<split>\n");
        for (int i = 0; i < constraintInfo.length; i++) {
            constraintInfo[i] = new GraphicInfo();
            constraintInfo[i].name = data[i * 3];
            constraintInfo[i].description = data[i * 3 + 1];
            constraintInfo[i].image = loader.ReadImage(data[i * 3 + 2]);
        }
    }

    public static GraphicsManager getInstance() {
        if (instance == null)
            instance = new GraphicsManager();
        return instance;
    }

    public Class[] GetConstraintClasses() {
        return constraintsClasses;
    }

    public Class[] GetConstraintGraphicsClasses() {
        return constraintGraphicsClasses;
    }

    public Class[] GetGraphicsClasses() {
        return graphicsClasses;
    }

    public GraphicInfo[] GetGraphicsInfo()
    {
        return graphicInfo;
    }

    public GraphicInfo[] GetConstraintsInfo()
    {
        return constraintInfo;
    }
}
