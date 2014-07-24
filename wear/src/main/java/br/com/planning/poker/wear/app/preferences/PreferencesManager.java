package br.com.planning.poker.wear.app.preferences;

import android.content.Context;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import br.com.planning.poker.wear.R;

public class PreferencesManager {
	private static final Class<?> array = R.array.class;
	
	/**
	 * Seta na SharedPreferences o deck escolhido pelo usuario
	 * @param deckName
	 * @param context
	 */
	public static void setDeck(String deckName, Context context) {
		SharedPreferencesWrapper.getEditor().putString(context.getString(R.string.deck_key), deckName).commit();
	}
	
	/**
	 * Recupera as cartas do deck selecionado ou o default caso contrario.
	 * @param context
	 * @return
	 */
	public static ArrayList<String> getDeckArrayList(Context context) {
		ArrayList<String> deck = new ArrayList<String>();

		for(String card : getDeckAsArray(context)) {
			deck.add(card);
		}

		return deck;
	}
	
	/**
	 * Recupera as cartas do deck selecionado ou o default caso contrario.
	 * @param context
	 * @return
	 * @see br.com.planning.poker.wear.app.preferences.PreferencesManager#getDeckArrayList(android.content.Context context)
	 */
	public static String[] getDeckAsArray(Context context) {
		String deckName = getDeckSelectedValue(context);
		
		return getDeckByNameAsArray(context, deckName);
	}
	
	/**
	 * Recupera as cartas do deck pelo nome
	 * @param context
	 * @param name
	 * @return
	 * @see br.com.planning.poker.wear.app.preferences.PreferencesManager#getDeckArrayList(android.content.Context context)
	 * @see br.com.planning.poker.wear.app.preferences.PreferencesManager#getDeckAsArray(android.content.Context context)
	 */
	public static String[] getDeckByNameAsArray(Context context, String name) {
		Integer id = getDeckArrayId(name);
		if(id == null) {
			id = getDeckArrayId(context.getString(R.string.deck_default));
		}
		return context.getResources().getStringArray(id);
	}
	
	public static String getDeckNameByIndex(Context context, int index) {
		String[] decks = getDeckEntryValues(context);
		return decks[index];
	}
	
	/**
	 * Recupera as cartas do deck pelo nome
	 * @param context
	 * @param name
	 * @return
	 * @see br.com.planning.poker.wear.app.preferences.PreferencesManager#getDeckArrayList(android.content.Context context)
	 * @see br.com.planning.poker.wear.app.preferences.PreferencesManager#getDeckAsArray(android.content.Context context)
	 */
	public static ArrayList<String> getDeckByNameAsArrayList(Context context, String name) {
		Integer id = getDeckArrayId(name);
		
		if(id == null) {
			id = getDeckArrayId(context.getString(R.string.deck_default));
		}
		
		ArrayList<String> deck = new ArrayList<String>();
		
		for(String card : getDeckByNameAsArray(context, name)) {
			deck.add(card);
		}
		
		return deck;
	}
	
	public static int getDeckSelectedValueIndex(Context context) {
		String selectedDeckValue = getDeckSelectedValue(context);
		return getDeckPositionByName(context, selectedDeckValue);
	}
	
	/**
	 * Retorna o nome do deck selecionado ou o default caso contrario.
	 * @param context
	 * @return
	 */
	public static String getDeckSelectedValue(Context context) {
		String selectedDeck = null;
		if(null != (selectedDeck = SharedPreferencesWrapper.getString(context.getString(R.string.deck_key)))) {
			return selectedDeck;
		}
		return context.getString(R.string.deck_default);
	}
	
	/**
	 * Retorna a descricao do Deck (Nome Legivel)
	 * @param context
	 * @param deck
	 * @return
	 */
	public static String getDeckDescriptionByName(Context context, String deck) {
		String[] descriptions = context.getResources().getStringArray(R.array.deck_description);
		return descriptions[getDeckPositionByName(context, deck)];
	}
	
	public static int getDeckPositionByName(Context context, String deck) {
		int i = 0;

		for(String deckName : getDeckEntryValues(context)) {
			if(deckName.equals(deck)) {
				return i;
			}
			i++;
		}
		
		return -1;
	}

	public static String[] getDeckEntries(Context context) {
		boolean hasCustomDeck = hasCustomDeck(context);
		String[] deckValues = getDeckEntryValues(context);
		String[] deckNames = getDeckNames(context);
		String[] deckEntries = new String[hasCustomDeck ? deckNames.length : deckNames.length - 1];
		
		String deckDescription = null;
		
		int i = 0;

		for(String s : deckValues) {
			String[] cards = getDeckByNameAsArray(context, s);

			if(! hasCustomDeck && "custom_deck".equals(s)) {
				continue;
			}

			if(cards.length > 0) {
				deckDescription = Arrays.toString(cards).replaceAll("\\[|\\]", "");
				deckEntries[i] = context.getString(R.string.deck_name_description, (Object) deckNames[i++], (Object) deckDescription);
			} else {
				deckEntries[i] = context.getString(R.string.deck_name_description_empty, deckNames[i++]);
			}
		}
		
		return deckEntries;
	}
	
	/**
	 * Retorna a lista de decks disponiveis para um ListPreference
	 * @param context
	 * @return
	 */
	public static String[] getDeckEntryValues(Context context) {
		return context.getResources().getStringArray(R.array.deck_list);
	}
	
	public static String[] getDeckNames(Context context) {
		return context.getResources().getStringArray(R.array.deck_description);
	}

	/**
	 * Retorna o nome do deck dado pela posicao dele
	 * @param context
	 * @param position
	 * @return
	 */
	//public static String getDeckName(Context context, Integer position) {
	//	return getDeckEntryValues(context)[position];
	//}
	
	/**
	 * Retorna o id no Resource do deck
	 * @param deck
	 * @return
	 */
	private static Integer getDeckArrayId(String deck) {
		try {
			Field field = array.getField(deck);
			return field.getInt(null);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getCardBackgroundColor(Context context) {
		return SharedPreferencesWrapper.getInt(context.getString(R.string.background_color_key), context.getResources().getColor(R.color.card_background));
	}

	public static void setCardBackgroundColor(Context context, int color) {
		SharedPreferencesWrapper.getEditor().putInt(context.getString(R.string.background_color_key), color).commit();
	}

	public static int getCardTextColor(Context context) {
		return SharedPreferencesWrapper.getInt(context.getString(R.string.text_color_key), context.getResources().getColor(R.color.card_text));
	}

	public static void setCardTextColor(Context context, int color) {
		SharedPreferencesWrapper.getEditor().putInt(context.getString(R.string.text_color_key), color).commit();
	}

	public static void setCustomDeck(Context context, String deckRepresentation) {
		SharedPreferencesWrapper.getEditor().putString(context.getString(R.string.custom_deck_key), deckRepresentation).commit();
	}

	public static boolean hasCustomDeck(Context context) {
		return SharedPreferencesWrapper.contains(context.getString(R.string.custom_deck_key));
	}

	public static String getCustomDeck(Context context) {
		return SharedPreferencesWrapper.getString(context.getString(R.string.custom_deck_key));
	}

	public static ArrayList<String> getCustomDeckAsArrayList(Context context) {
		String customDeck = getCustomDeck(context);

		if(customDeck == null) {
			return null;
		}

		ArrayList<String> cards = new ArrayList<String>();

		for(String card : customDeck.split(",")) {
			cards.add(card);
		}

		return cards;
	}
}
