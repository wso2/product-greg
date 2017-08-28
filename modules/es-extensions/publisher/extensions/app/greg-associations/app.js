app.server = function(ctx){
    return {
        endpoints:{
            pages:[
                {
                    title:'Associations',
                    url:'associations',
                    path:'associations.jag',
                    permission: 'ASSET_ASSOCIATIONS',
                    secured:true
                }
            ]
        }
    };
};

app.pageHandlers = function(ctx) {
    return {
        onPageLoad: function() {
            if ((ctx.isAnonContext) && (ctx.endpoint.secured)) {
                ctx.res.sendRedirect(ctx.appContext+'/login');
                return false;
            }
            return true;
        }
    };
};

app.renderer = function(ctx){
    return {
        pageDecorators:{
            populateAssociationTypes:function(page){
                page.associations = ['a','b','c'];
            }
        }
    }
};