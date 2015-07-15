var render = function(theme,data,meta,require) {
	theme('2-column-right',{
		title:'WSO2 Governance Center - Store',
		header:[{
			partial:'header',
			context:data
		}],
		body:[{
			partial:'gc-landing',
			context:data
		}
		]
	});
};