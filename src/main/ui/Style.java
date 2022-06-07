package main.ui;

import main.FileLoader;
import main.ObjectsHandler;
import main.ParameterHolder;
import main.ui.graphics.UIImage;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Function;

public class Style {

    private static Style instance;
    private ObjectsHandler handler;

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    //region Position and Scale

    private int width, height;
    private double scale;

    public enum Anchor {
        Upper_Left(-1, -1),
        Top(0, -1),
        Upper_Right(1, -1),
        Left(-1, 0),
        Center(0, 0),
        Right(1, 0),
        Lower_Left(-1, 1),
        Bottom(0, 1),
        Lower_Right(1, 1);

        private int x, y;

        Anchor(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public void SetSize(int width, int height) {
        this.width = width;
        this.height = height;
        scale = width / (double) WIDTH;
        if (scale > height / (double) HEIGHT)
            scale = height / (double) HEIGHT;
    }

    public Point2D.Double GetPoint(Point2D.Double point, Anchor anchor)
    {
        Point2D.Double result = new Point2D.Double();
        double x = point.x;
        double y = point.y;
        x *= scale;
        y *= scale;
        result.x = x + width * (anchor.x + 1) / 2.0;
        result.y = y + height * (anchor.y + 1) / 2.0;
        return result;
    }

    public UITransform GetTransform(UITransform transform, Anchor anchor) {
        UITransform result = new UITransform();
        double x = transform.x;
        double y = transform.y;
        x *= scale;
        y *= scale;
        result.x = x + width * (anchor.x + 1) / 2.0;
        result.y = y + height * (anchor.y + 1) / 2.0;
        result.width = transform.width*scale;
        result.height = transform.height*scale;
        return result;
    }
    public double GetScaled(double value)
    {
        return value*scale;
    }
    //endregion

    //region Colors
    private Color[] colors;
    public enum ColorPalette
    {
        Body1(2), Body2(7), Body3(12), Accent1(17), Accent2(22), Special(27),
        Dark(2), SemiDark(1), Medium(0), SemiLight(-1), Light(-2);

        public int value;

        ColorPalette(int value)
        {
            this.value = value;
        }
    }
    public Color GetColor(int ind)
    {
        return colors[ind];
    }
    public void SetButtonColors(MenuButton button, int ind)
    {
        SetButtonColor(button, ButtonColorSets[ind][0], ButtonColorSets[ind][1]);
    }
    private Color tintColor(Color color, double tint)
    {
        return new Color((int)(color.getRed()*tint), (int)(color.getGreen()*tint), (int)(color.getBlue()*tint));
    }
    public void SetButtonColor(MenuButton button, int main, int text)
    {
        Color[] cols = new Color[4];
        cols[0] = GetColor(main);
        cols[1] = tintColor(cols[0], 0.92);
        cols[2] = tintColor(cols[0], 0.75);
        cols[3] = GetColor(text);
        button.SetColors(cols);
    }
    //TODO And new button color sets
    public int[][] ButtonColorSets =
    {
        {
            ColorPalette.Body2.value+ColorPalette.Light.value,
            ColorPalette.Body1.value+ColorPalette.Dark.value
        },
        {
            ColorPalette.Body2.value+ColorPalette.SemiDark.value,
            ColorPalette.Body1.value+ColorPalette.Light.value
        },
        {
            ColorPalette.Body3.value+ColorPalette.Light.value,
            ColorPalette.Body1.value+ColorPalette.Dark.value
        },
        {
            ColorPalette.Body3.value+ColorPalette.SemiDark.value,
            ColorPalette.Body1.value+ColorPalette.Light.value
        },
        {
            ColorPalette.Accent1.value+ColorPalette.SemiLight.value,
            ColorPalette.Body1.value+ColorPalette.Dark.value
        },
        {
            ColorPalette.Accent2.value+ColorPalette.SemiLight.value,
            ColorPalette.Body1.value+ColorPalette.Dark.value
        }
    };
    public void LoadPalette(FileLoader loader)
    {
        BufferedImage palette = loader.ReadImage("palette.png");
        int width = palette.getWidth(null), height = palette.getHeight(null);
        colors = new Color[width*height];
        int ind = 0;
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                colors[ind++] = new Color(palette.getRGB(i, j));
            }
        }
    }
    //endregion

    //region Fonts
    public static Font[] fonts =
    {
            new Font("Franklin Gothic Book", Font.PLAIN, 48),
            new Font("Century Gothic", Font.PLAIN, 48),
            new Font("Franklin Gothic Book", Font.PLAIN, 54),
            new Font("Franklin Gothic Book", Font.ITALIC, 40),
            new Font("Century Gothic", Font.PLAIN, 25),
            new Font("Franklin Gothic Book", Font.PLAIN, 25),
            new Font("Franklin Gothic Book", Font.ITALIC, 20),
            new Font("Century Gothic", Font.PLAIN, 30),
            new Font("Franklin Gothic Book", Font.ITALIC, 25)
    };
    public Font GetFont(int ind)
    {
        return fonts[ind].deriveFont((float)(fonts[ind].getSize()*scale));
    }
    //endregion

    //region Shapes ans Patterns

    //TODO add shapes for basic menu icons
    private Shape[] shapes, patterns;
    private Shape GetTextShape(String text)
    {
        Font font = GetFont(1).deriveFont(70f);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        TextLayout tx = new TextLayout(text, font, frc);
        int w = tx.getPixelBounds(frc, 0f, 0f).width, h = tx.getPixelBounds(frc, 0f, 0f).height;
        GlyphVector v = font.createGlyphVector(frc, text);
        Area a = new Area(v.getOutline());
        a.transform(AffineTransform.getTranslateInstance(-w/2.0, h/2.0));
        return a;
    }
    public void GenerateShapes()
    {
        System.out.println("Generating Shapes & Patterns");
        ArrayList<Shape> shapes = new ArrayList<>();
        {
            Rectangle2D.Double rect1 = new Rectangle2D.Double(-50, -2.5, 100, 5);
            Rectangle2D.Double rect2 = new Rectangle2D.Double(-2.5, -50, 5, 100);
            Area a1 = new Area(rect1);
            a1.add(new Area(rect2));
            a1.transform(AffineTransform.getRotateInstance(Math.toRadians(45), 0, 0));
            shapes.add(a1);
        }
        {
            Ellipse2D.Double circ1 = new Ellipse2D.Double(-40, -40, 80, 80);
            Ellipse2D.Double circ2 = new Ellipse2D.Double(-35, -35, 70, 70);
            Area a = new Area(circ1);
            a.subtract(new Area(circ2));
            shapes.add(a);
        }
        {
            for(char c = 'A'; c <= 'Z'; c++)
            {
                shapes.add(GetTextShape(String.valueOf(c)));
            }
            char[] chars = new char[]{
                    '?', '!', '#', '$', '+', '-', '%', '&', '>', '<'
            };
            for(char c : chars)
            {
                shapes.add(GetTextShape(String.valueOf(c)));
            }
        }
        this.shapes = shapes.toArray(Shape[]::new);

        shapes = new ArrayList<>();
        {
            Area a = new Area();
            int[][] x = new int[][]
            {
                {-50, -25, -50},
                {-50, 0, 25, -50},
                {-50, 50, 50, -25},
                {0, 50, 50, 25}
            };
            int[][] y = new int[][]
            {
                    {-50, -50, -25},
                    {0, -50, -50, 25},
                    {50, -50, -25, 50},
                    {50, 0, 25, 50}
            };
            for(int i = 0; i < 4; i++)
            {
                Polygon polygon = new Polygon(x[i], y[i], x[i].length);
                a.add(new Area(polygon));
            }
            shapes.add(a);
        }
        {
            Area a = new Area();
            int[][] x = new int[][]
            {
                    {50, 25, 50},
                    {50, 0, -25, 50},
                    {50, -50, -50, 25},
                    {0, -50, -50, -25}
            };
            int[][] y = new int[][]
            {
                    {-50, -50, -25},
                    {0, -50, -50, 25},
                    {50, -50, -25, 50},
                    {50, 0, 25, 50}
            };
            for(int i = 0; i < 4; i++)
            {
                Polygon polygon = new Polygon(x[i], y[i], x[i].length);
                a.add(new Area(polygon));
            }
            shapes.add(a);
        }
        {
            Area a = new Area();
            for(int x = 0; x < 4; x++)
            {
                for(int y = 0; y < 4; y++)
                {
                    if((x+y)%2 == 1)
                        continue;
                    Rectangle2D.Double rect = new Rectangle2D.Double(25*x-50, 25*y-50, 25, 25);
                    a.add(new Area(rect));
                }
            }
            shapes.add(a);
        }
        {
            Polygon polygon = new Polygon(new int[]{-50, 0, 50, 0}, new int[]{0, -50, 0, 50}, 4);
            Area a = new Area();
            for(int x = -1; x <= 1; x++)
            {
                for(int y = -1; y <= 1; y++)
                {
                    Area diamond = new Area(polygon);
                    diamond.transform(AffineTransform.getTranslateInstance(x*100, y*100));
                    a.add(diamond);
                }
            }
            a.transform(AffineTransform.getScaleInstance(1/3.0, 1/3.0));
            shapes.add(a);
        }
        {
            Area a = new Area();
            Ellipse2D.Double circle = new Ellipse2D.Double(-15, -15, 30, 30);
            Rectangle2D.Double square = new Rectangle2D.Double(-25, -25, 50, 50);
            Area a1 = new Area(circle);
            a1.transform(AffineTransform.getTranslateInstance(-25, -25));
            Area a2 = new Area(square);
            a2.subtract(new Area(circle));
            a2.transform(AffineTransform.getTranslateInstance(25, -25));
            Area a3 = new Area(square);
            a3.subtract(new Area(circle));
            a3.transform(AffineTransform.getTranslateInstance(-25, 25));
            Area a4 = new Area(circle);
            a4.transform(AffineTransform.getTranslateInstance(25, 25));
            a.add(a1);
            a.add(a2);
            a.add(a3);
            a.add(a4);
            shapes.add(a);
        }
        {
            double thickness = 10.0;
            Ellipse2D.Double circleBig = new Ellipse2D.Double(-50.0-thickness/2.0, -50-thickness/2.0, 100+thickness, 100+thickness);
            Ellipse2D.Double circleSmall = new Ellipse2D.Double(-50.0+thickness/2.0, -50+thickness/2.0, 100-thickness, 100-thickness);
            Rectangle2D.Double square = new Rectangle2D.Double(-50.0, -50.0, 100.0, 100.0);
            Area circle = new Area(circleBig);
            circle.subtract(new Area(circleSmall));
            circle.intersect(new Area(square));
            Area a1 = new Area(circle), a2 = new Area(circle), a3 = new Area(circle), a4 = new Area(circle);
            a1.transform(AffineTransform.getTranslateInstance(-50, -50));
            a2.transform(AffineTransform.getTranslateInstance(50, -50));
            a3.transform(AffineTransform.getTranslateInstance(-50, 50));
            a4.transform(AffineTransform.getTranslateInstance(50, 50));
            circle.add(a1);
            circle.add(a2);
            circle.add(a3);
            circle.add(a4);
            circle.intersect(new Area(square));
            shapes.add(circle);
        }
        {
            Rectangle2D.Double square = new Rectangle2D.Double(-20, -20, 40, 40);
            Area a = new Area();
            for(int x = 0; x < 2; x++)
            {
                for(int y = 0; y < 2; y++)
                {
                    Area sq = new Area(square);
                    sq.transform(AffineTransform.getTranslateInstance((x-0.5)*50, (y-0.5)*50));
                    a.add(sq);
                }
            }
            shapes.add(a);
        }
        {
            Polygon diamond = new Polygon(new int[]{-25, 0, 25, 0}, new int[]{0, -50, 0, 50}, 4);
            Rectangle2D.Double rect = new Rectangle2D.Double(-12.5, -25, 25, 50);
            double thickness = 10.0;
            Area rectArea = new Area(rect);
            Area diamondBig = new Area(diamond), diamondSmall = new Area(diamond);
            diamondBig.transform(AffineTransform.getScaleInstance((50.0+thickness)/100.0, (50.0+thickness)/100.0));
            diamondSmall.transform(AffineTransform.getScaleInstance((50.0-thickness)/100.0, (50.0-thickness)/100.0));
            diamondBig.subtract(diamondSmall);
            diamondBig.intersect(rectArea);
            Area a = new Area();
            for(int i = 0; i < 4; i++)
            {
                for(int j  = 0; j < 2; j++)
                {
                    Area diam = new Area(diamondBig);
                    diam.transform(AffineTransform.getTranslateInstance((i-1.5)*25.0, (j-0.5)*50.0));
                    a.add(diam);
                }
            }
            shapes.add(a);
        }
        this.patterns = shapes.toArray(Shape[]::new);
    }
    public Shape GetShape(int index, double size)
    {
        if(index < 0 || index >= shapes.length)
            return null;
        Area a = new Area(shapes[index]);
        double scale = size/100.0;
        a.transform(AffineTransform.getScaleInstance(scale, scale));
        return a;
    }
    public Shape GetPattern(int index, double size)
    {
        if(index < 0 || index >= patterns.length)
            return null;
        Area a = new Area(patterns[index]);
        double scale = size/100.0;
        a.transform(AffineTransform.getScaleInstance(scale, scale));
        return a;
    }
    public int GetShapesCount()
    {
        return shapes.length;
    }
    public int GetPatternsCount()
    {
        return patterns.length;
    }
    //endregion

    //region UI Presets

    private static class InputFieldPreset
    {
        public double roundness, thickness, fontCoefficient, placeholderCoefficient;
        public int textFieldColor, buttonColors, textColor, font, placeHolderFont;

        public InputFieldPreset(double roundness, double thickness, double fontCoefficient, double placeholderCoefficient,
                                int textFieldColor, int buttonColors, int textColor, int font, int placeHolderFont)
        {
            this.roundness = roundness;
            this.thickness = thickness;
            this.fontCoefficient = fontCoefficient;
            this.placeholderCoefficient = placeholderCoefficient;
            this.textFieldColor = textFieldColor;
            this.buttonColors = buttonColors;
            this.textColor = textColor;
            this.font = font;
            this.placeHolderFont = placeHolderFont;
        }
    }
    private InputFieldPreset[] inputFieldPresets = new InputFieldPreset[]
    {
            new InputFieldPreset(20.0, 5, 0.5, 0.4,
                    ColorPalette.Body2.value-2, 1, ColorPalette.Body2.value+2, 0, 3)
    };
    public void SetInputField(MenuInputField field, double height, int index) {
        InputFieldPreset preset = inputFieldPresets[index];
        field.SetRoundness(GetScaled(preset.roundness));
        field.SetTextFieldColor(GetColor(preset.textFieldColor));
        field.SetThickness(GetScaled(preset.thickness));
        MenuButton fieldButton = field.getButton();
        SetButtonColors(fieldButton, preset.buttonColors);
        fieldButton.SetText("");
        MenuLabel fieldLabel = field.getLabel();
        fieldLabel.SetColor(GetColor(preset.textColor));
        field.SetFonts(GetFont(preset.font).deriveFont((float) (height * preset.fontCoefficient)),
                GetFont(preset.placeHolderFont).deriveFont((float) (height * preset.placeholderCoefficient)));
    }

    public void SetHandler(ObjectsHandler handler)
    {
        this.handler = handler;
    }

    private double parameterYOffset;
    private double paramStartYOffset = 150, paramStartXOffset = -500;
    private Anchor parametersAnchor = Anchor.Upper_Right;
    private MenuGroup parameterWindow;
    public void StartParameters()
    {
        parameterYOffset = paramStartYOffset;
        parameterWindow = new MenuGroup();
    }

    public void SetParametersOffset(double offsetX, double offsetY)
    {
        paramStartXOffset = offsetX;
        paramStartYOffset = parameterYOffset = offsetY;
    }
    public void SetParametersAnchor(Anchor anchor)
    {
        parametersAnchor = anchor;
    }

    public void ParameterButton(String name, Runnable action)
    {
        MenuButton button = new MenuButton(GetTransform(new UITransform(paramStartXOffset+100, parameterYOffset+20, 200, 35),
                parametersAnchor), GetScaled(10.0));
        SetButtonColors(button, 0);
        button.SetTextStyle(GetFont(5));
        button.SetText(name);
        button.SetAction(action, 0);
        parameterYOffset += 40.0;

        handler.AddObject(button);
        parameterWindow.AddObject(button);
    }

    public MenuParameter<Integer> GetIntegerParameter(MenuView view, ParameterHolder<Integer> parameter, String parameterName, int minValue, int maxValue, int defaultValue)
    {
        Point2D.Double pos = GetPoint(new Point2D.Double(paramStartXOffset, parameterYOffset), parametersAnchor);
        MenuLabel label = new MenuLabel(pos.x, pos.y);
        label.SetAlignment(MenuLabel.Alignment.Upper_Left);
        label.SetFont(GetFont(4));
        label.SetColor(GetColor(ColorPalette.Body3.value-1));
        label.SetText(parameterName);
        parameterYOffset += 30.0;

        MenuInputField inputField = new MenuInputField(GetTransform(new UITransform(paramStartXOffset+100, parameterYOffset+20, 200, 35),
                parametersAnchor));
        inputField.SetThickness(GetScaled(5.0));
        inputField.SetRoundness(GetScaled(10.0));
        inputField.SetTextFieldColor(GetColor(ColorPalette.Body1.value-2));
        MenuButton fieldButton = inputField.getButton();
        SetButtonColors(fieldButton, 0);
        inputField.SetPlaceholder("Enter Integer");
        inputField.SetFonts(GetFont(5), GetFont(6));
        inputField.SetText(parameter.Get().toString());
        inputField.SetMultiline(true);
        MenuParameter<Integer> param = new MenuParameter<Integer>() {
            private int value = parameter.Get();
            public void SetParameterValue(Integer val)
            {
                value = val;
                if(value < minValue)
                    value = minValue;
                else if(value > maxValue)
                    value = maxValue;
                parameter.Set(value);
                inputField.SetText(((Integer)value).toString());
            }
            public Integer GetParameterValue() {
                return value;
            }
        };

        inputField.SetSelectAction(() -> view.SelectInputField(inputField));
        inputField.SetDeselectAction(() -> {
            String value = inputField.GetText();
            if(value == null || value.equals("")) {
                param.SetParameterValue(defaultValue);
                return;
            }
            int num = 0, st = 0;
            boolean rev = false;
            if(value.charAt(0) == '-')
            {
                st++; rev = true;
            }
            for(int i = st; i < value.length(); i++)
            {
                char c = value.charAt(i);
                if(!Character.isDigit(c))
                {
                    param.SetParameterValue(defaultValue);
                    return;
                }
                num = num*10 + (c-'0');
            }
            if(rev)
                num = -num;
            param.SetParameterValue(num);
            view.DeselectInputField();
        });

        MenuButton decButton = new MenuButton(GetTransform(new UITransform(paramStartXOffset+220, parameterYOffset+20, 35, 35),
                parametersAnchor), GetScaled(10.0));
        SetButtonColors(decButton, 0);
        decButton.SetTextStyle(GetFont(5));
        decButton.SetText("-");
        decButton.SetAction(() -> {
            int value = param.GetParameterValue()-1;
            if(value < minValue)
                value = minValue;
            param.SetParameterValue(value);
        }, 0);

        MenuButton addButton = new MenuButton(GetTransform(new UITransform(paramStartXOffset+260, parameterYOffset+20, 35, 35),
                parametersAnchor), GetScaled(10.0));
        SetButtonColors(addButton, 0);
        addButton.SetTextStyle(GetFont(5));
        addButton.SetText("+");
        addButton.SetAction(() -> {
            int value = param.GetParameterValue()+1;
            if(value > maxValue)
                value = maxValue;
            param.SetParameterValue(value);
        }, 0);

        MenuButton defButton = new MenuButton(GetTransform(new UITransform(paramStartXOffset+300, parameterYOffset+20, 35, 35),
                parametersAnchor), GetScaled(10.0));
        SetButtonColors(defButton, 0);
        defButton.SetTextStyle(GetFont(5));
        defButton.SetText("x");
        defButton.SetAction(() -> {
            param.SetParameterValue(defaultValue);
        }, 0);

        parameterYOffset += 40;

        handler.AddObject(label);
        handler.AddObject(inputField);
        handler.AddObject(addButton);
        handler.AddObject(decButton);
        handler.AddObject(defButton);

        parameterWindow.AddObject(label);
        parameterWindow.AddObject(inputField);
        parameterWindow.AddObject(addButton);
        parameterWindow.AddObject(decButton);
        parameterWindow.AddObject(defButton);

        return param;
    }

    public MenuParameter<Double> GetDoubleParameter(MenuView view, ParameterHolder<Double> parameter, String parameterName, double minValue, double maxValue, int precision)
    {
        Point2D.Double pos = GetPoint(new Point2D.Double(paramStartXOffset, parameterYOffset), parametersAnchor);
        MenuLabel label = new MenuLabel(pos.x, pos.y);
        label.SetAlignment(MenuLabel.Alignment.Upper_Left);
        label.SetFont(GetFont(4));
        label.SetColor(GetColor(ColorPalette.Body3.value-1));
        label.SetText(parameterName);
        parameterYOffset += 30;

        Point2D.Double pos1 = GetPoint(new Point2D.Double(paramStartXOffset+10, parameterYOffset+20.0), parametersAnchor),
                pos2 = GetPoint(new Point2D.Double(paramStartXOffset+210, parameterYOffset+20.0), parametersAnchor);
        MenuSlider slider = new MenuSlider(pos1.x, pos1.y, pos2.x, pos2.y);
        slider.SetThickness(GetScaled(5.0), GetScaled(10.0));
        slider.SetColor(GetColor(ColorPalette.Body2.value-1));
        DragCircle square = slider.GetDrag();
        square.SetActions(()->handler.SetDrag(square), ()->handler.SetDrag(null));
        square.SetSize(GetScaled(25));
        square.SetColor(GetColor(ColorPalette.Body2.value));

        StringBuilder formatString = new StringBuilder("#.");
        formatString.append("#".repeat(precision));
        DecimalFormat format = new DecimalFormat(formatString.toString(), DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        format.setRoundingMode(RoundingMode.DOWN);

        MenuInputField inputField = new MenuInputField(GetTransform(new UITransform(paramStartXOffset+310, parameterYOffset+20, 150, 35),
                parametersAnchor));
        inputField.SetThickness(GetScaled(5.0));
        inputField.SetRoundness(GetScaled(10.0));
        inputField.SetTextFieldColor(GetColor(ColorPalette.Body1.value-2));
        MenuButton fieldButton = inputField.getButton();
        SetButtonColors(fieldButton, 0);
        inputField.SetPlaceholder("Enter Value");
        inputField.SetFonts(GetFont(5), GetFont(6));
        inputField.SetText(format.format(parameter.Get()));

        parameterYOffset += 40;

        MenuParameter<Double> param = new MenuParameter<>() {

            public Double GetParameterValue() {
                return parameter.Get();
            }
            public void SetParameterValue(Double value) {
                if(value < minValue)
                    value = minValue;
                else if(value > maxValue)
                    value = maxValue;
                parameter.Set(value );
                slider.SetValue((value-minValue)/(maxValue-minValue));
                inputField.SetText(format.format(value));
            }
        };
        param.SetParameterValue(parameter.Get());

        inputField.SetSelectAction(() -> view.SelectInputField(inputField));
        inputField.SetDeselectAction(() -> {
            Double val;
            try {
                val = Double.parseDouble(inputField.GetText());
            } catch (Exception e) {
                val = minValue;
            }
            param.SetParameterValue(val);
            view.DeselectInputField();
        });
        slider.SetAction((val) -> param.SetParameterValue(minValue + (maxValue-minValue)*val));

        handler.AddObject(label);
        handler.AddObject(inputField);
        handler.AddObject(slider);

        parameterWindow.AddObject(label);
        parameterWindow.AddObject(inputField);
        parameterWindow.AddObject(slider);

        return param;
    }

    public MenuParameter<String> GetStringParameter(MenuView view, ParameterHolder<String> parameter, String parameterName, Function<Character, Boolean> checker)
    {
        Point2D.Double pos = GetPoint(new Point2D.Double(paramStartXOffset, parameterYOffset), parametersAnchor);
        MenuLabel label = new MenuLabel(pos.x, pos.y);
        label.SetAlignment(MenuLabel.Alignment.Upper_Left);
        label.SetFont(GetFont(4));
        label.SetColor(GetColor(ColorPalette.Body3.value-1));
        label.SetText(parameterName);
        parameterYOffset += 30.0;

        MenuInputField inputField = new MenuInputField(GetTransform(new UITransform(paramStartXOffset+150, parameterYOffset+30, 300, 50),
                parametersAnchor));
        inputField.SetThickness(GetScaled(5.0));
        inputField.SetRoundness(GetScaled(10.0));
        inputField.SetTextFieldColor(GetColor(ColorPalette.Body1.value-2));
        MenuButton fieldButton = inputField.getButton();
        SetButtonColors(fieldButton, 0);
        inputField.SetPlaceholder("Enter Text");
        inputField.SetFonts(GetFont(5), GetFont(6));
        inputField.SetText(parameter.Get());
        if(checker != null) {
            inputField.SetChecker(checker);
        }
        parameterYOffset += 60.0;

        MenuParameter<String> param = new MenuParameter<String>() {
            public String GetParameterValue() {
                return parameter.Get();
            }
            public void SetParameterValue(String value) {
                parameter.Set(value);
                inputField.SetText(value);
            }
        };

        inputField.SetSelectAction(() -> view.SelectInputField(inputField));
        inputField.SetDeselectAction(() -> {
            String value = inputField.GetText();
            if(checker != null) {
                for(int i = 0; i < value.length(); i++)
                {
                    if(!checker.apply(value.charAt(i)))
                    {
                        value = ""; break;
                    }
                }
            }
            param.SetParameterValue(value);
            view.DeselectInputField();
        });

        handler.AddObject(label);
        handler.AddObject(inputField);

        parameterWindow.AddObject(label);
        parameterWindow.AddObject(inputField);

        return param;
    }

    public MenuParameter<String> GetMultiStringParameter(MenuView view, ParameterHolder<String> parameter, String parameterName, Function<Character, Boolean> checker, int lines)
    {
        Point2D.Double pos = GetPoint(new Point2D.Double(paramStartXOffset, parameterYOffset), parametersAnchor);
        MenuLabel label = new MenuLabel(pos.x, pos.y);
        label.SetAlignment(MenuLabel.Alignment.Upper_Left);
        label.SetFont(GetFont(4));
        label.SetColor(GetColor(ColorPalette.Body3.value-1));
        label.SetText(parameterName);
        parameterYOffset += 30.0;

        double height = 35.0*lines+25.0;
        MenuInputField inputField = new MenuInputField(GetTransform(new UITransform(paramStartXOffset+150, parameterYOffset+height/2.0, 300, height-10.0),
                parametersAnchor));
        inputField.SetThickness(GetScaled(5.0));
        inputField.SetRoundness(GetScaled(10.0));
        inputField.SetTextFieldColor(GetColor(ColorPalette.Body1.value-2));
        MenuButton fieldButton = inputField.getButton();
        SetButtonColors(fieldButton, 0);
        inputField.SetPlaceholder("Enter Text");
        inputField.SetFonts(GetFont(5), GetFont(6));
        inputField.SetText(parameter.Get());
        if(checker != null) {
            inputField.SetChecker(checker);
        }
        inputField.SetMultiline(true);
        parameterYOffset += height;

        MenuParameter<String> param = new MenuParameter<String>() {
            public String GetParameterValue() {
                return parameter.Get();
            }
            public void SetParameterValue(String value) {
                parameter.Set(value);
                inputField.SetText(value);
            }
        };

        inputField.SetSelectAction(() -> view.SelectInputField(inputField));
        inputField.SetDeselectAction(() -> {
            String value = inputField.GetText();
            if(checker != null) {
                for(int i = 0; i < value.length(); i++)
                {
                    if(!checker.apply(value.charAt(i)))
                    {
                        value = ""; break;
                    }
                }
            }
            param.SetParameterValue(value);
            view.DeselectInputField();
        });

        handler.AddObject(label);
        handler.AddObject(inputField);

        parameterWindow.AddObject(label);
        parameterWindow.AddObject(inputField);

        return param;
    }

    public MenuParameter<Point> GetPointParameter(ParameterHolder<Point> parameter, Point2D.Double gridPosition, Point2D.Double cellSize,  Point gridSize)
    {
        DragPoint rect = new DragPoint();
        rect.SetSize(15, 15);
        rect.SetColor(GetColor(ColorPalette.Body1.value+2));
        rect.SetColors(GetColor(ColorPalette.Body1.value-2), GetColor(ColorPalette.Body3.value-1), GetColor(ColorPalette.Body3.value));
        rect.SetThickness(GetScaled(4.0));
        rect.SetActions(() -> handler.SetDrag(rect), () -> handler.SetDrag(null));
        rect.SetRenderPriority(63);
        MenuParameter<Point> param = new MenuParameter<>() {
            public Point GetParameterValue() {
                return parameter.Get();
            }
            public void SetParameterValue(Point value) {
                parameter.Set(value);
                rect.SetPos(value.x*cellSize.x + gridPosition.x, value.y*cellSize.y + gridPosition.y);
            }
        };
        rect.SetHost((x, y) -> {
            Point p = new Point((int)Math.round((x-gridPosition.x)/cellSize.x), (int)Math.round((y-gridPosition.y)/cellSize.y));
            p.x = Math.min(p.x, gridSize.x);
            p.x = Math.max(p.x, 0);
            p.y = Math.min(p.y, gridSize.y);
            p.y = Math.max(p.y, 0);
            param.SetParameterValue(p);
        });
        param.SetParameterValue(parameter.Get());

        handler.AddObject(rect);
        parameterWindow.AddObject(rect);
        return param;
    }

    public MenuParameter<Boolean> GetBooleanParameter(ParameterHolder<Boolean> parameter, String parameterName)
    {
        Point2D.Double pos = GetPoint(new Point2D.Double(paramStartXOffset, parameterYOffset), parametersAnchor);
        MenuLabel label = new MenuLabel(pos.x, pos.y);
        label.SetAlignment(MenuLabel.Alignment.Upper_Left);
        label.SetFont(GetFont(4));
        label.SetColor(GetColor(ColorPalette.Body3.value-1));
        label.SetText(parameterName);
        parameterYOffset += 30.0;

        MenuButton toggleButton = new MenuButton(GetTransform(new UITransform(paramStartXOffset+20, parameterYOffset+20, 35, 35), parametersAnchor),
                GetScaled(10.0));
        toggleButton.SetText("");

        pos = GetPoint(new Point2D.Double(paramStartXOffset+40, parameterYOffset+20), parametersAnchor);
        MenuLabel toggleLabel = new MenuLabel(pos.x, pos.y);
        toggleLabel.SetAlignment(MenuLabel.Alignment.Left);
        toggleLabel.SetFont(GetFont(5));
        toggleLabel.SetColor(GetColor(ColorPalette.Body3.value-1));
        parameterYOffset += 40;

        MenuParameter<Boolean> param = new MenuParameter<Boolean>() {
            public Boolean GetParameterValue() {
                return parameter.Get();
            }
            public void SetParameterValue(Boolean value) {
                parameter.Set(value);
                if (!value) {
                    SetButtonColors(toggleButton, 0);
                    toggleLabel.SetText("No");
                } else {
                    SetButtonColors(toggleButton, 1);
                    toggleLabel.SetText("Yes");
                }

            }
        };
        param.SetParameterValue(parameter.Get());
        toggleButton.SetAction(() -> param.SetParameterValue(!param.GetParameterValue()), 0);

        handler.AddObject(label);
        handler.AddObject(toggleButton);
        handler.AddObject(toggleLabel);

        parameterWindow.AddObject(label);
        parameterWindow.AddObject(toggleButton);
        parameterWindow.AddObject(toggleLabel);

        return param;
    }

    private float[] toHSV(Color c)
    {
        float[] RGBA = new float[4], HSV = new float[4];
        c.getRGBComponents(RGBA);

        Color.RGBtoHSB((int)(RGBA[0]*255), (int)(RGBA[1]*255), (int)(RGBA[2]*255), HSV);
        HSV[3] = RGBA[3];
        return HSV;
    }

    private Color setAlpha(Color color, int value)
    {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), value);
    }
    private Color toRGB(float[] HSV)
    {
        Color col = new Color(Color.HSBtoRGB(HSV[0], HSV[1], HSV[2]));
        return setAlpha(col, (int)(HSV[3]*255));
    }

    private BufferedImage GenerateSVSquare(int width, int height, float hue)
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for(int j = 0; j < height; j++)
        {
            float val = 1-(float)j / height;
            for(int i = 0; i < width; i++)
            {
                float sat = (float)i / width;
                Color col = toRGB(new float[]{hue, sat, val, 1});
                image.setRGB(i, j, col.getRGB());
            }
        }
        return image;
    }

    private BufferedImage GenerateHueBand(int width, int height)
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for(int j = 0; j < height; j++)
        {
            float val = 1-(float)j / height;
            Color col = toRGB(new float[]{val, 1, 1, 1});
            for(int i = 0; i < width; i++)
            {
                image.setRGB(i, j, col.getRGB());
            }
        }
        return image;
    }

    private BufferedImage GenerateAlphaBand(int width, int height)
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for(int j = 0; j < height; j++)
        {
            int value = (255*j/height);
            Color col = new Color(value, value, value);
            for(int i = 0; i < width; i++)
            {
                image.setRGB(i, j, col.getRGB());
            }
        }
        return image;
    }
    public MenuParameter<Color> GetColorParameter(ParameterHolder<Color> parameter, String parameterName)
    {
        return GetColorParameter(parameter, parameterName, true);
    }
    public MenuParameter<Color> GetColorParameter(ParameterHolder<Color> parameter, String parameterName, boolean hasAlpha)
    {
        Point2D.Double pos = GetPoint(new Point2D.Double(paramStartXOffset, parameterYOffset), parametersAnchor);
        MenuLabel label = new MenuLabel(pos.x, pos.y);
        label.SetAlignment(MenuLabel.Alignment.Upper_Left);
        label.SetFont(GetFont(4));
        label.SetColor(GetColor(ColorPalette.Body3.value-1));
        label.SetText(parameterName);
        parameterYOffset += 35.0;

        UITransform SVTransform = GetTransform(new UITransform(paramStartXOffset, parameterYOffset, 200, 200), parametersAnchor),
        HueTransform = GetTransform(new UITransform(paramStartXOffset+205, parameterYOffset, 30, 200), parametersAnchor),
        AlphaTransform = GetTransform(new UITransform(paramStartXOffset+240, parameterYOffset, 30, 200), parametersAnchor);

        ColorPicker SVImage = new ColorPicker(SVTransform.x, SVTransform.y);
        SVImage.SetTransform(SVTransform);
        SVImage.SetDraw(true, true);
        SVImage.SetDragActions(() -> handler.SetDrag(SVImage), () -> handler.SetDrag(null));

        ColorPicker HueImage = new ColorPicker(HueTransform.x, HueTransform.y);
        HueImage.SetTransform(HueTransform);
        HueImage.SetDraw(false, true);
        HueImage.SetImage(GenerateHueBand((int)HueTransform.width, (int)HueTransform.height));
        HueImage.SetDragActions(() -> handler.SetDrag(HueImage), () -> handler.SetDrag(null));

        ColorPicker AlphaImage = new ColorPicker(AlphaTransform.x, AlphaTransform.y);
        if(hasAlpha) {
            AlphaImage.SetTransform(AlphaTransform);
            AlphaImage.SetDraw(false, true);
            AlphaImage.SetImage(GenerateAlphaBand((int) AlphaTransform.width, (int) AlphaTransform.height));
            AlphaImage.SetDragActions(() -> handler.SetDrag(AlphaImage), () -> handler.SetDrag(null));
        }

        MenuParameter<Color> param = new MenuParameter<>() {
            public Color GetParameterValue() {
                return parameter.Get();
            }

            public void SetParameterValue(Color value) {
                parameter.Set(value);
            }
        };

        SVImage.SetAction((x, y) -> {
         //   System.out.println(param.GetParameterValue());
            float[] HSV = new float[4];
            if(hasAlpha)
                HSV[3] = (float)(AlphaImage.GetSelect().y/ AlphaTransform.height);
            else
                HSV[3] = 1;
            HSV[2] = 1-(float)(y/ HueTransform.height);
            HSV[1] = (float)(x/ SVTransform.width);
            HSV[0] = 1-(float)(HueImage.GetSelect().y/ HueTransform.height);
            //System.out.println((float)(y/ SVTransform.y) + " " + (float)(x/ SVTransform.x));
            param.SetParameterValue(toRGB(HSV));
        });

        HueImage.SetAction((x, y) -> {
            float[] HSV = toHSV(param.GetParameterValue());
            HSV[0] = 1-(float)(y/ HueTransform.height);
            param.SetParameterValue(toRGB(HSV));
            SVImage.SetImage(GenerateSVSquare((int)SVTransform.width, (int)SVTransform.height, 1-(float)(y/ HueTransform.height)));
        });

        if(hasAlpha)
            AlphaImage.SetAction((x, y) -> {
                param.SetParameterValue(setAlpha(param.GetParameterValue(), (int)(255*y/ AlphaTransform.height)));
            });

        float[] HSV = toHSV(parameter.Get());
        SVImage.Select(SVTransform.width*HSV[1], SVTransform.height*(1-HSV[2]));
        HueImage.Select(0, HueTransform.height*(1-HSV[0]));
        if(hasAlpha) {
            AlphaImage.Select(0, AlphaTransform.height * HSV[3]);
        }
        SVImage.SetImage(GenerateSVSquare((int)SVTransform.width, (int)SVTransform.height, HSV[0]));

        parameterYOffset += 205;

        handler.AddObject(label);
        handler.AddObject(SVImage);
        handler.AddObject(HueImage);

        parameterWindow.AddObject(label);
        parameterWindow.AddObject(SVImage);
        parameterWindow.AddObject(HueImage);

        if(hasAlpha) {
            handler.AddObject(AlphaImage);
            parameterWindow.AddObject(AlphaImage);
        }

        return param;
    }

    public MenuGroup GetParameterWindow()
    {
        return parameterWindow;
    }
    public MenuGraphic GetGraphicButton(String name)
    {
        MenuGraphic graphic = new MenuGraphic(GetTransform(new UITransform(300, 100, 400, 35), Anchor.Upper_Left));
        graphic.SetColor(GetColor(ColorPalette.Body2.value-1));
        graphic.SetRoundness(GetScaled(10.0));
        graphic.SetPadding(GetScaled(5.0));
        MenuLabel label = graphic.GetLabel();
        label.SetFont(GetFont(5));
        label.SetColor(GetColor(ColorPalette.Body2.value+2));
        label.SetText(name);
        MenuButton editButton = graphic.GetEditButton();
        SetButtonColors(editButton, 0);
        editButton.SetRoundness(GetScaled(10.0));
        MenuButton toggleButton = graphic.GetToggleButton();
        SetButtonColors(toggleButton, 0);
        toggleButton.SetRoundness(GetScaled(10.0));
        MenuButton deleteButton = graphic.GetDeleteButton();
        SetButtonColors(deleteButton, 5);
        deleteButton.SetRoundness(GetScaled(10.0));
        return graphic;
    }

    public MenuConstraint GetConstraintButton(String name)
    {
        MenuConstraint constraint = new MenuConstraint(GetTransform(new UITransform(300, 100, 400, 35), Anchor.Upper_Left));
        constraint.SetColor(GetColor(ColorPalette.Body3.value-1));
        constraint.SetRoundness(GetScaled(10.0));
        constraint.SetPadding(GetScaled(5.0));
        MenuLabel label = constraint.GetLabel();
        label.SetFont(GetFont(5));
        label.SetColor(GetColor(ColorPalette.Body1.value-2));
        label.SetText(name);
        MenuButton editButton = constraint.GetEditButton();
        SetButtonColors(editButton, 2);
        editButton.SetRoundness(GetScaled(10.0));
        MenuButton toggleButton = constraint.GetToggleButton();
        SetButtonColors(toggleButton, 2);
        toggleButton.SetRoundness(GetScaled(10.0));
        MenuButton deleteButton = constraint.GetDeleteButton();
        SetButtonColors(deleteButton, 5);
        deleteButton.SetRoundness(GetScaled(10.0));
        return constraint;
    }

    //endregion

    private Style()
    {
        scale = 1.0;
        colors = new Color[1];
    }
    public static Style getInstance()
    {
        if(instance == null)
            instance = new Style();
        return instance;
    }
}
