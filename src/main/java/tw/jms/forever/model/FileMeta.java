package tw.jms.forever.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileMeta {
	private String name;
	private String path;
	@JsonProperty("isDir")
	private boolean isDir;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDir() {
		return isDir;
	}

	public void setIsDir(boolean isDir) {
		this.isDir = isDir;
	}
}
