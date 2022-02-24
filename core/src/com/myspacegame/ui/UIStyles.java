package com.myspacegame.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.myspacegame.Info;
import com.myspacegame.MainClass;

public class UIStyles {


    private final TextButton.TextButtonStyle textButtonStyle;
    private final Label.LabelStyle labelStyle;
    private final Label.LabelStyle titleLabelStyle;

    public UIStyles(MainClass app) {
        textButtonStyle = new TextButton.TextButtonStyle();
        labelStyle = new Label.LabelStyle();
        titleLabelStyle = new Label.LabelStyle();

        textButtonStyle.font = app.assetManager.get("DoppioOne.ttf");
        textButtonStyle.fontColor = Info.colorCyan;
        textButtonStyle.overFontColor = Info.colorBlueDarken4;
        textButtonStyle.downFontColor = Info.colorRed;

        labelStyle.font = app.assetManager.get("DoppioOneText.ttf");
        labelStyle.fontColor = Info.colorBlue;

        titleLabelStyle.font = app.assetManager.get("DoppioOneTitle.ttf");
        titleLabelStyle.fontColor = Info.colorBlueDarken4;

    }

    public TextButton.TextButtonStyle getTextButtonStyle() {
        return textButtonStyle;
    }

    public Label.LabelStyle getLabelStyle() {
        return labelStyle;
    }

    public Label.LabelStyle getTitleLabelStyle() {
        return titleLabelStyle;
    }

    public ImageButton.ImageButtonStyle createImageButtonStyle(Texture up, Texture over, Texture down) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
//        style.imageUp = new NinePatchDrawable(up);
        style.imageUp = new TextureRegionDrawable(up);
        style.imageOver = new TextureRegionDrawable(over);
        style.imageDown = new TextureRegionDrawable(down);
        return style;
    }
}
