import { AfterViewInit, Directive, ElementRef, OnDestroy, Renderer2 } from '@angular/core';

@Directive({
	selector: 'p-table[appResponsiveTable]',
	standalone: false
})
export class ResponsiveTableDirective implements AfterViewInit, OnDestroy {
	private observer: MutationObserver | null = null;

	constructor(private readonly host: ElementRef<HTMLElement>, private readonly renderer: Renderer2) {}

	ngAfterViewInit(): void {
		this.renderer.addClass(this.host.nativeElement, 'mobile-stack-table');
		this.updateCellLabels();
		if (typeof MutationObserver !== 'undefined') {
			this.observer = new MutationObserver(() => this.updateCellLabels());
			this.observer.observe(this.host.nativeElement, { childList: true, characterData: true, subtree: true });
		}
	}

	ngOnDestroy(): void {
		this.observer?.disconnect();
	}

	private updateCellLabels(): void {
		const table = this.host.nativeElement.querySelector<HTMLTableElement>('.p-datatable-table');
		if (!table) return;
		const headers = Array.from(table.querySelectorAll('thead tr:last-child th'))
			.map((header) => header.textContent?.replace(/\s+/g, ' ').trim() ?? '');
		if (!headers.length) return;

		for (const row of Array.from(table.querySelectorAll('tbody tr'))) {
			const cells = Array.from(row.querySelectorAll(':scope > td'));
			if (cells.length === 1 && cells[0].hasAttribute('colspan')) continue;
			cells.forEach((cell, index) => {
				const label = headers[index];
				if (!label) return;
				let labelElement = cell.querySelector<HTMLElement>(
					':scope > .p-datatable-column-title, :scope > .mobile-stack-label');
				if (!labelElement) {
					labelElement = this.renderer.createElement('span');
					this.renderer.addClass(labelElement, 'mobile-stack-label');
					this.renderer.setAttribute(labelElement, 'aria-hidden', 'true');
					this.renderer.insertBefore(cell, labelElement, cell.firstChild);
				}
				if (!labelElement) return;
				if (labelElement.textContent !== label) this.renderer.setProperty(labelElement, 'textContent', label);
			});
		}
	}
}
