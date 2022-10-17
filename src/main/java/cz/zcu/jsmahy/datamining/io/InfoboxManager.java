package cz.zcu.jsmahy.datamining.io;

import cz.zcu.jsmahy.datamining.data.infobox.InfoboxTemplate;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfoboxManager {
	public static final String INFOBOX_URL = "https://en.wikipedia.org/wiki/Wikipedia:List_of_infoboxes";
	public static final Pattern INFOBOX_NAME_PATTERN = Pattern.compile("[Ii]nfobox ([\\w\\d ]+)");
	// TODO there can be "|" in the key-value, fix that
	// e.g.
    /*
        | language         =
        | discipline       = <!-- or |genre= -->
                                     ^
                                    /|\
                                     |
     */
	public static final Pattern KEY_VALUE_PATTERN = Pattern.compile("([\\w\\d]+)[ ]*=[ ]*([ |\"'<!->\\w\\d]*)");
	private static final Logger L = LogManager.getLogger();

	/**
	 * Downloads the infobox templates to the file with the given name in folder data/infobox/
	 *
	 * @param fileName the name of the file
	 *
	 * @throws IllegalArgumentException if the file name is not valid
	 */
	public static void downloadTemplates(final String fileName) throws IllegalArgumentException {
		IOValidator.validateFileName(fileName);

		final Service<Void> s = new InfoboxDownload(fileName);
		s.setOnSucceeded(x -> {
			System.out.println("Done");
			L.info("Successfully downloaded infoboxes");
		});
		s.setOnFailed(x -> {
			L.warn("Failed to download infoboxes, reason:");
			L.throwing(x.getSource()
			            .getException());
		});
		L.info(String.format("Downloading infobox data from %s", INFOBOX_URL));
		s.start();
	}

	/**
	 * Attempts to parse
	 *
	 * @param template
	 *
	 * @return
	 *
	 * @throws IllegalArgumentException
	 */
	public static InfoboxTemplate createInfoboxTemplate(final String template) throws IllegalArgumentException {
		if (template == null) {
			throw invalidTemplateException();
		}
		// TODO ^[ ]?\\|
		final String[] map = template.split("\\|");
		if (map.length <= 1) {
			throw invalidTemplateException();
		}
		// first part should be Infobox <name>
		final Matcher m = INFOBOX_NAME_PATTERN.matcher(map[0]);
		if (!m.find()) {
			throw invalidTemplateException();
		}
		final String infoboxName = m.group(1);

		final Collection<String> required = new HashSet<>();
		final Collection<String> optional = new HashSet<>();
		for (int i = 1; i < map.length; i++) {
			final Matcher kvm = KEY_VALUE_PATTERN.matcher(map[i]);
			if (!kvm.find()) {
				continue;
			}
			final String key = kvm.group(1);
			if (kvm.groupCount() >= 2 && kvm.group(2)
			                                .toLowerCase()
			                                .contains("required")) {
				required.add(key);
			} else {
				optional.add(key);
			}
		}
		return new InfoboxTemplate(infoboxName, required, optional);
	}

	private static IllegalArgumentException invalidTemplateException() {
		return new IllegalArgumentException("Invalid template format");
	}

	private static class InfoboxDownload extends Service<Void> {
		public static final String TITLE = "Template:Infobox";
		public static final int AMOUNT = Integer.MAX_VALUE;
		private final String fileName;

		private InfoboxDownload(String fileName) {
			this.fileName = fileName;
		}

		@Override
		protected final Task<Void> createTask() {
			return new Task<>() {
				@Override
				protected Void call() throws Exception {

					final Document doc = Jsoup.connect(INFOBOX_URL)
					                          .get();
					// infoboxes contain their name in the title tag
					final Elements templates = doc.select(String.format("a[title*=%s]", TITLE));

					// set the initial capacity high as there are many infoboxes
					final Collection<InfoboxTemplate> infoboxTemplates = new HashSet<>(7919);
					int curr = 0;
					for (final Element el : templates) {
						// get the absolute hyperlink reference and connect to the page
						final String pageURL = el.attr("abs:href");
						final Document page = Jsoup.connect(pageURL)
						                           .get();

						// the infobox data is in the first "<pre>"
						final Element usage = page.select("pre")
						                          .first();
						if (usage == null) {
							continue;
						}

						final String data = usage.text();
						try {
							infoboxTemplates.add(createInfoboxTemplate(data));
							curr++;
							if (curr == AMOUNT) {
								break;
							}
						} catch (Exception e) {
							L.throwing(e);
						}
					}
					IOManager.saveInfoboxData(fileName, infoboxTemplates);
					return null;
				}
			};
		}
	}
}
