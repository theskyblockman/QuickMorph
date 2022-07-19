package fr.theskyblockman.quickmorph.morphing;

import fr.theskyblockman.quickmorph.ActionableListener;

public class SoulListener implements ActionableListener {
    public Morph morph;
    private boolean activated;
    public SoulListener(Morph morph) {
        this.morph = morph;
    }

    @Override
    public void setActivated(boolean value) {
        activated = value;
    }

    @Override
    public boolean isActivated() {
        return activated;
    }
}
