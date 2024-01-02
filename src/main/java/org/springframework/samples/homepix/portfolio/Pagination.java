package org.springframework.samples.homepix.portfolio;

import java.util.ArrayList;
import java.util.List;

public class Pagination {

	private List<String> sortOptions;

	public Pagination() {

		sortOptions = new ArrayList<>();

		sortOptions.add("Default Sort");
		sortOptions.add("Sort by Title");
		sortOptions.add("Sort by Filename");
		sortOptions.add("Sort by Date");
		sortOptions.add("Sort by Size");
		sortOptions.add("Sort by Aspect Ratio");
	}

	public boolean hasNext() {
		return true;
	}

	public boolean hasPrevious() {
		return true;
	}

	public boolean paginated() {
		return true;
	}

	public int pageCount() {
		return 20;
	}

	public int page() {
		return 6;
	}

	public int nextPage() {
		return (page() + 1) % pageCount();
	}

	public int previousPage() {
		return (page() + pageCount() - 1) % pageCount();
	}

	public int caseOption(int l) {
		if (l == page())
			return 0;
		else if (l == 1)
			return 1;
		else if (l == 2) {
			if (page() > 5)
				return 2;
			else
				return 3;
		}
		else if (l <= pageCount() + 3 && l >= pageCount() - 3) {
			if (page() == l)
				return 4;
			else
				return 5;
		}
		else if (l == pageCount() - 1) {
			if (page() < pageCount() - 5)
				return 6;
			else
				return 7;
		}
		else if (l == pageCount()) {
			return 8;
		}

		return -1;
	}

	public List<String> sortOptions() {
		return sortOptions;
	}

}
