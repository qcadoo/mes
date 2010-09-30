var QCD = QCD || {};
QCD.menu = QCD.menu || {};

QCD.menu.MenuModel = function(menuItems) {
	
	this.selectedItem = null;
	
	this.items = new Array();
	this.itemsMap = new Object();
	for (var i in menuItems) {
		var button = new QCD.menu.FirstButton(menuItems[i]);
		this.items.push(button);
		this.itemsMap[button.name] = button;
		if (! this.selectedItem) {
			this.selectedItem = button;
		}
	}
}

QCD.menu.FirstButton = function(menuItem) {
	this.name = menuItem.name;
	this.label = menuItem.label;
	
	this.element = null;
	
	this.selectedItem = null;
	
	this.itemsMap = new Object();
	this.items = new Array();
	for (var i in menuItem.items) {
		var secondButton = new QCD.menu.SecondButton(menuItem.items[i], this);
		this.itemsMap[secondButton.name] = secondButton;
		this.items.push(secondButton);
		if (! this.selectedItem) {
			this.selectedItem = secondButton;
		}
	}
	if (this.items.length == 0) {
		this.ribbon = new QCD.menu.RibbonModel(this.name, menuItem.ribbonItems);
	}
}

QCD.menu.SecondButton = function(menuItem, firstButton) {
	this.name = firstButton.name+"_"+menuItem.name;
	this.label = menuItem.label;
	
	this.element = null;
	
	this.ribbon = new QCD.menu.RibbonModel(this.name, menuItem.ribbonItems);
}

QCD.menu.RibbonModel = function(parentName, ribbonItems) {
	this.name = parentName+"_ribbon";
	this.items = ribbonItems;
}

/*var menuStructure = {
menuItems: [
	{
		name: "home",
		label: "home",
		ribbonItems: [
			{
				name: "testgroup1",
				items: [
					{name: "big1", type: "bigButton", label: "b1", clickAction: "goToView:products.testViewB1"},
					{name: "big2", type: "bigButton"},
					{name: "small1", type: "smallButton", label:"s1", clickAction: "goToView:products.testViewS1"},
					{name: "small2", type: "smallButton"},
					{name: "small3", type: "smallButton"},
					{name: "small4", type: "smallButtonDropdown", label:"s4", clickAction: "goToView:products.testViewS4", items: [
						{name: "small4-1", label:"s41", clickAction: "goToView:products.testViewS41"},
						{name: "small4-2"}
					]},
					{name: "small5 das dsa das da", type: "smallButtonDropdown", items: [
							{name: "small5-1"},
							{name: "small5-2"}
						]},
						{name: "small6", type: "smallButton"},
					{name: "big3", type: "bigButton"},
					{name: "big4", type: "bigButtonDropdown", label:"B4", clickAction: "goToView:products.testViewB4", items: [
						{name: "item1", label:"B41", clickAction: "goToView:products.testViewB41"},
						{name: "item2"},
						{name: "item3"},
						{name: "item4"}
					]},
					{name: "big5", type: "bigButtonDropdown", items: [
							{name: "item5-1"},
							{name: "item5-2"},
							{name: "item5-3"}
						]}
				]
			}
		]
		
	},
	{
		name: "test1",
		label: "test1",
		items: [
			{name: "test1-1", label: "test1-1"},
			{name: "test1-2", label: "test1-2",
				ribbonItems: [
					{
						name: "testgroup1",
						items: [
							{name: "big1", type: "bigButton"},
							{name: "big2", type: "bigButton"},
							{name: "small1", type: "smallButton"},
							{name: "small2", type: "smallButton"},
							{name: "small3", type: "smallButton"},
							{name: "big3", type: "bigButton"}
						]
					},
					{
						name: "testgroup2",
						items: [
							{name: "small4", type: "smallButton"},
							{name: "small5", type: "smallButton"},
							{name: "big4", type: "bigButton"}
						]
					}
				]
			},
			{name: "test1-3", label: "test1-3"}
		]
	},
	{
		name: "test2",
		label: "test2",
		items: [
			{name: "test2-1", label: "test2-1"},
			{name: "test2-2", label: "test2-2"}
		]
	},
	{
		name: "test3",
		label: "test3"
	},
	{
		name: "products",
		label: "produkty",
		ribbonItems: [
			{
				name: "navigation",
				items: [
					{name: "list", label: "lista produktow", type: "bigButton"},
					{name: "new", label: "nowy produkt", type: "smallButton"},
					{name: "edit", label: "edytuj produkt", type: "smallButton"}
				]
			},
			{
				name: "actions",
				items: [
					{name: "save", label: "zapisz", type: "bigButton"},
					{name: "saveClose", label: "zapisz i zamknij", type: "bigButton"},
					{name: "saveNew", label: "zapisz i nowy", type: "smallButton"},
					{name: "cancel", label: "anuluj", type: "smallButton"},
					{name: "delete", label: "usun", type: "smallButton"}
				]
			}
		]
	}
]
};*/

var menuStructure = {
	menuItems: [
		{
			name: "home",
			label: "home",
			ribbonItems: [
				{
					name: "home",
					items: [
						{name: "dashboard", type: "bigButton", label: "dashboard"}
					]
				}
			]
		},
		{
			name: "products",
			label: "Zarządzanie Produktami",
			items: [
				{name: "products", label: "Produkty", ribbonItems: [
						{
							name: "navigation",
							items: [
								{name: "list", label: "lista", type: "bigButton"},
								{name: "new", label: "nowy", type: "bigButton"}
								//{name: "edit", label: "edytuj", type: "smallButton"}
							]
						},
						{
							name: "actions1",
							items: [
								{name: "save", label: "zapisz", type: "bigButton"},
								{name: "saveClose", label: "zapisz i zamknij", type: "bigButton"},
								{name: "saveNew", label: "zapisz i nowy", type: "smallButton"},
								{name: "cancel", label: "anuluj", type: "smallButton"},
								{name: "delete", label: "usun", type: "smallButton"}
							]
						},
						{
							name: "actions2",
							items: [
								{name: "edit", label: "edytuj", type: "bigButton"},
								{name: "delete", label: "usun", type: "smallButton"}
							]
						}
					]},
				{name: "instructions", label: "Instrukcje materiałowe", ribbonItems: [
					{
							name: "navigation",
							items: [
								{name: "list", label: "lista", type: "bigButton"},
								{name: "new", label: "nowy", type: "smallButton"},
								{name: "edit", label: "edytuj", type: "smallButton"}
							]
						},
						{
							name: "actions",
							items: [
								{name: "save", label: "zapisz", type: "bigButton"},
								{name: "saveClose", label: "zapisz i zamknij", type: "bigButton"},
								{name: "saveNew", label: "zapisz i nowy", type: "smallButton"},
								{name: "cancel", label: "anuluj", type: "smallButton"},
								{name: "delete", label: "usun", type: "smallButton"}
							]
						}
					]}
			]
		},
		{
			name: "orders",
			label: "Zlecenia produkcyjne",
			ribbonItems: [
				{
					name: "navigation",
					items: [
						{name: "list", label: "lista", type: "bigButton"},
						{name: "new", label: "nowy", type: "smallButton"},
						{name: "edit", label: "edytuj", type: "smallButton"}
					]
				},
				{
					name: "actions",
					items: [
						{name: "save", label: "zapisz", type: "bigButton"},
						{name: "saveClose", label: "zapisz i zamknij", type: "bigButton"},
						{name: "saveNew", label: "zapisz i nowy", type: "smallButton"},
						{name: "cancel", label: "anuluj", type: "smallButton"},
						{name: "delete", label: "usun", type: "smallButton"}
					]
				}
			]
		},
		{
			name: "dictionaries",
			label: "Słowniki",
			ribbonItems: [
				{
					name: "navigation",
					items: [
						{name: "list", label: "lista", type: "bigButton"},
						{name: "values", label: "pokaz wartosci", type: "smallButton"}
					]
				},
				{
					name: "actions",
					items: [
						{name: "save", label: "zapisz", type: "bigButton"},
						{name: "saveClose", label: "zapisz i zamknij", type: "bigButton"},
						{name: "saveNew", label: "zapisz i nowy", type: "smallButton"},
						{name: "cancel", label: "anuluj", type: "smallButton"},
						{name: "delete", label: "usun", type: "smallButton"}
					]
				}
			]
		},
		{
			name: "administration",
			label: "Administracja",
			items: [
				{name: "users", label: "Użytkownicy i Grupy"},
				{name: "plugins", label: "Pluginy"}
			]
		}
	]
};