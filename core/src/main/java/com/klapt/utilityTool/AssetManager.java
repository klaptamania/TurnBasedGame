package com.klapt.utilityTool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.HashMap;

public class AssetManager {
    HashMap<String, Texture> textures = new HashMap<>();
    Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

    public AssetManager(){
        getAllTexture();
    }

    private void getAllTexture() {
        textures.put("Temper", new Texture(Gdx.files.internal("Entity/temper.png")));
        textures.put("Person", new Texture(Gdx.files.internal("Entity/person.png")));
        textures.put("BGTemper", new Texture(Gdx.files.internal("Background/psor.png")));
        textures.put("Bone", new Texture(Gdx.files.internal("Entity/bone.png")));

        //Item
        textures.put("Straight Sword", new Texture(Gdx.files.internal("Image/Weapon/Medium/straightSword.png")));
        textures.put("Wooden Staff", new Texture(Gdx.files.internal("Image/Weapon/Super Light/woodenStaff.png")));
        textures.put("Chef's Knife", new Texture(Gdx.files.internal("Image/Weapon/Light/chefKnife.png")));
        textures.put("Katana", new Texture(Gdx.files.internal("Image/Weapon/Medium/katana.png")));
        textures.put("Butcher Sword", new Texture(Gdx.files.internal("Image/Weapon/Heavy/butcherSword.png")));


        //Armor
        textures.put("Wizard Hat", new Texture(Gdx.files.internal("Image/Armor/Head/wizardHat.png")));
        textures.put("Iron Chestplate", new Texture(Gdx.files.internal("Image/Armor/Body/iron_chestplate.png")));
        textures.put("Iron Leggings", new Texture(Gdx.files.internal("Image/Armor/Leg/iron_leggings.png")));
        textures.put("Iron Helm", new Texture(Gdx.files.internal("Image/Armor/Head/iron_helm.png")));
        textures.put("Iron Boots", new Texture(Gdx.files.internal("Image/Armor/Feet/ironBoots.png")));
        textures.put("Iron Gauntlet", new Texture(Gdx.files.internal("Image/Armor/Arm/iron_gauntlet.png")));

        //Animation
        textures.put("BA1", new Texture(Gdx.files.internal("Ani/Basic Attack/frame_001.png")));
        textures.put("BA2", new Texture(Gdx.files.internal("Ani/Basic Attack/frame_002.png")));
        textures.put("BA3", new Texture(Gdx.files.internal("Ani/Basic Attack/frame_003.png")));
        textures.put("BA4", new Texture(Gdx.files.internal("Ani/Basic Attack/frame_004.png")));
        textures.put("BA5", new Texture(Gdx.files.internal("Ani/Basic Attack/frame_005.png")));

        textures.put("SeveredBleedSheet", new Texture(Gdx.files.internal("Ani/Effect/SeveredBleedSheet.png")));
        textures.put("BleedSheet", new Texture(Gdx.files.internal("Ani/Effect/BleedSheet.png")));
        textures.put("FireBallSheet", new Texture(Gdx.files.internal("Ani/Effect/FireBall.png")));
        textures.put("DodgeSheet", new Texture(Gdx.files.internal("Ani/Effect/Dodge.png")));
        textures.put("SelfHealSheet", new Texture(Gdx.files.internal("Ani/Effect/SelfHeal.png")));
        textures.put("IgnitedSheet", new Texture(Gdx.files.internal("Ani/Effect/Ignited.png")));
        textures.put("ThunderboltSheet", new Texture(Gdx.files.internal("Ani/Effect/thunderbolt.png")));
        textures.put("IaidoSheet", new Texture(Gdx.files.internal("Ani/Effect/Iaido.png")));
        textures.put("ReflectingSheet", new Texture(Gdx.files.internal("Ani/Effect/reflecting.png")));
        textures.put("ReflectSheet", new Texture(Gdx.files.internal("Ani/Effect/reflect.png")));
        textures.put("STRBuffSheet", new Texture(Gdx.files.internal("Ani/Effect/strBuffed.png")));
        textures.put("STRUnBuffSheet", new Texture(Gdx.files.internal("Ani/Effect/strDebuffed.png")));
        textures.put("STRBuffActivateSheet", new Texture(Gdx.files.internal("Ani/Effect/strBuffActivate.png")));
        textures.put("ThrustSheet", new Texture(Gdx.files.internal("Ani/Effect/thrust.png")));
        textures.put("QuickSnapSheet", new Texture(Gdx.files.internal("Ani/Effect/quickSnap.png")));
        textures.put("ReviveSheet", new Texture(Gdx.files.internal("Ani/Effect/revive.png")));

        //UI
        textures.put("HealthGrayBar", new Texture(Gdx.files.internal("Image/UI/grayHealthBar.png")));
        textures.put("HealthBar", new Texture(Gdx.files.internal("Image/UI/healthBar.png")));
    }

    public Texture getTexture(String name){
        return textures.get(name);
    }
    public Skin getSkin() {return skin;}

    public void dispose(){
        for(var t : textures.values()){
            t.dispose();
        }

        skin.dispose();
    }
}
