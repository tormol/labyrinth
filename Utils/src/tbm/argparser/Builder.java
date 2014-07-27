package tbm.argparser;

import java.util.function.Consumer;

public class Builder {
	public String shortopt_regex = "[A-Za-z].*";
	public boolean nonopt_stops_opts = false;
	public boolean h_help = true;
	public Consumer<String> errors =
		new Consumer<String>(){public void accept(String str) {
			System.err.println(str);
			System.exit(-1);
		}};
	public Builder()
		{}
	public Core parse(String... args) {
		return new Core(this,  args);
	}
	public Builder valid_shortopts(String regex) {
		shortopt_regex = regex;
		return this;
	}
	public Builder integer_shortopts() {
		shortopt_regex = "[A-Za-Z0-9].*";
		return this;
	}
	public Builder h_not_help() {
		h_help = false;
		return this;
	}
	public Builder errors(Consumer<String> errors) {
		this.errors = errors;
		return this;
	}
}
