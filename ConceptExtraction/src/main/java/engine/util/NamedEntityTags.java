package engine.util;

public enum NamedEntityTags {

	LOCATION("LOCATION"), PERSON("PERSON"), ORGANIZATION("ORGANIZATION"), DATE(
			"DATE"), NUMBER("NUMBER"), TIME("TIME"), PERCENT("PERCENT");

	private final String tag;

	private NamedEntityTags(String tag) {
		this.tag = tag;
	}

	public String toString() {
		return getTag();
	}

	protected String getTag() {
		return this.tag;
	}

	public static NamedEntityTags get(String value) {
		for (NamedEntityTags v : values()) {
			if (value.equals(v.getTag())) {
				return v;
			}
		}

		return null;
	}

}