/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.game.cost;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.gui.input.InputSelectCardsFromList;
import forge.util.Lang;

/**
 * The Class CostReveal.
 */
public class CostReveal extends CostPartWithList {
    // Reveal<Num/Type/TypeDescription>
    
    /**
     * Instantiates a new cost reveal.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @param description
     *            the description
     */
    public CostReveal(final String amount, final String type, final String description) {
        super(amount, type, description);
    }

    @Override
    public boolean isReusable() { return true; }

    @Override
    public boolean isRenewable() { return true; }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#canPay(forge.card.spellability.SpellAbility,
     * forge.Card, forge.Player, forge.card.cost.Cost)
     */
    @Override
    public final boolean canPay(final SpellAbility ability) {
        final Player activator = ability.getActivatingPlayer();
        final Card source = ability.getSourceCard();
        
        List<Card> handList = new ArrayList<Card>(activator.getCardsIn(ZoneType.Hand));
        final String type = this.getType();
        final Integer amount = this.convertAmount();

        if (this.payCostFromSource()) {
            if (!source.isInZone(ZoneType.Hand)) {
                return false;
            }
        } else if (this.getType().equals("Hand")) {
            return true;
        } else if (this.getType().equals("SameColor")) {
            if (amount == null) {
                return false;
            } else {
                for (final Card card : handList) {
                    if (CardLists.filter(handList, new Predicate<Card>() {
                        @Override
                        public boolean apply(final Card c) {
                            return c.sharesColorWith(card);
                        }
                    }).size() >= amount) {
                        return true;
                    }
                }
                return false;
            }
        } else {
            if (ability.isSpell()) {
                handList.remove(source); // can't pay for itself
            }
            handList = CardLists.getValidCards(handList, type.split(";"), activator, source);
            if ((amount != null) && (amount > handList.size())) {
                // not enough cards in hand to pay
                return false;
            }
            System.out.println("revealcost - " + amount + type + handList);
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#payHuman(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final PaymentDecision payHuman(final SpellAbility ability, final Player activator) {
        final Card source = ability.getSourceCard();
        final String amount = this.getAmount();

        if (this.payCostFromSource())
            return PaymentDecision.card(source);

        if (this.getType().equals("Hand"))
            return PaymentDecision.card(activator.getCardsIn(ZoneType.Hand));

        InputSelectCardsFromList inp = null;
        if (this.getType().equals("SameColor")) {
            Integer num = this.convertAmount();
            List<Card> handList = activator.getCardsIn(ZoneType.Hand);
            final List<Card> handList2 = handList;
            handList = CardLists.filter(handList, new Predicate<Card>() {
                @Override
                public boolean apply(final Card c) {
                    for (Card card : handList2) {
                        if (!card.equals(c) && card.sharesColorWith(c)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            if (num == 0) 
                return PaymentDecision.number(0);

            inp = new InputSelectCardsFromList(num, handList) {
                private static final long serialVersionUID = 8338626212893374798L;

                @Override
                protected void onCardSelected(Card c, MouseEvent triggerEvent) {
                    Card firstCard = Iterables.getFirst(this.selected, null);
                    if(firstCard != null && !CardPredicates.sharesColorWith(firstCard).apply(c))
                        return;
                    super.onCardSelected(c, triggerEvent);
                }
            };
            inp.setMessage("Select " + Lang.nounWithAmount(num, "card" ) + " of same color to reveal.");

        } else {
            Integer num = this.convertAmount();

            List<Card> handList = activator.getCardsIn(ZoneType.Hand);
            handList = CardLists.getValidCards(handList, this.getType().split(";"), activator, ability.getSourceCard());

            if (num == null) {
                final String sVar = ability.getSVar(amount);
                if (sVar.equals("XChoice")) {
                    num = chooseXValue(source, ability, handList.size());
                } else {
                    num = AbilityUtils.calculateAmount(source, amount, ability);
                }
            }
            if ( num == 0 )
                return PaymentDecision.number(0);;
                
            inp = new InputSelectCardsFromList(num, num, handList);
            inp.setMessage("Select %d more " + getDescriptiveType() + " card(s) to reveal.");
        }
        inp.setCancelAllowed(true);
        inp.showAndWait();
        if (inp.hasCancelled())
            return null;
        
        return PaymentDecision.card(inp.getSelected());


    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Reveal ");

        final Integer i = this.convertAmount();

        if (this.payCostFromSource()) {
            sb.append(this.getType());
        } else if (this.getType().equals("Hand")) {
            return ("Reveal you hand");
        } else if (this.getType().equals("SameColor")) {
            return ("Reveal " + i + " cards from your hand that share a color");
        } else {
            final StringBuilder desc = new StringBuilder();

            if (this.getType().equals("Card")) {
                desc.append("Card");
            } else {
                desc.append(this.getTypeDescription() == null ? this.getType() : this.getTypeDescription()).append(
                        " card");
            }

            sb.append(Cost.convertAmountTypeToWords(i, this.getAmount(), desc.toString()));
        }
        sb.append(" from your hand");

        return sb.toString();
    }

    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#executePayment(forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    protected void doPayment(SpellAbility ability, Card targetCard) {
        targetCard.getGame().getAction().reveal(Lists.newArrayList(targetCard), ability.getActivatingPlayer());
    }

    
    @Override protected boolean canPayListAtOnce() { return true; }
    @Override
    protected void doListPayment(SpellAbility ability, Collection<Card> targetCards) {
        ability.getActivatingPlayer().getGame().getAction().reveal(targetCards, ability.getActivatingPlayer());
    }    
    
    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#getHashForList()
     */
    @Override
    public String getHashForList() {
        return "Revealed";
    }

    // Inputs
    public <T> T accept(ICostVisitor<T> visitor) {
        return visitor.visit(this);
    }


}
