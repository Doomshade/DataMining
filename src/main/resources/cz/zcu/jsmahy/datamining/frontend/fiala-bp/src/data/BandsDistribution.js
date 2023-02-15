define([ 
    'cz/kajda/timeline/render/BandItemRenderer', 
    'cz/kajda/timeline/render/SplitBandItemRenderer',
    'cz/kajda/timeline/render/DumbbellItemRenderer'
],

function(BandItemRenderer, SplitBandItemRenderer, DumbbellItemRenderer){

    var BandsDistribution = new Class("BandsDistribution", {
    
    _constructor : function() {
    },
    bands : [
        {
            id: "person",
            label: "Lidé",
            itemRenderer: new SplitBandItemRenderer("#FFB182"),
            color: "#fafafa"
        },           
        {
            id: "firm",
            label: "Firma",
            itemRenderer: new DumbbellItemRenderer("#B942F4"),
            color: "#fafafa"
        },
        {
            id: "place",
            label: "Místa",
            itemRenderer: new BandItemRenderer("#E0E810"),
            color: "#fafafa"
        },
        {
            id: "property",
            label: "Majetek",
            itemRenderer: new BandItemRenderer("#109CE8"),
            color: "#fafafa"
        },
           
       ],    
    });
    return BandsDistribution;
});