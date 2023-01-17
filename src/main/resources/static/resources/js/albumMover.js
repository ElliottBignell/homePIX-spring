////////////////////////////////////////////////////////////////
// Navigator for album page

function albumMover( dir ) 
{
    count = $('*').filter(function () {
        return this.id.match(/picture_\d+/); //regex for the pattern "img followed by a number"
    }).length;

    index = 1;
    this.settings = Array.apply( null, Array( count ) ).map( function(x, i) { return 0; } );
    this.picdir   = dir;

    objs = $( '#paneright' ).find( '.selectable' );

    // Call the parent's constructor
    mover.call(this);

    this.mapFun = function( obj, x, i ) 
        { 
            if ( !$('#div_' + ( i + 1 ) ).length ) { alert( "Failed on " + i ); }
            if ( !$('#keywords' + ( i + 1 ) ).length ) { alert( "Failed on " + i ); }

            var entry = {
                html: document.getElementById( 'div_'     + ( i + 1 ) ).innerHTML,
                keys: document.getElementById( 'keywords' + ( i + 1 ) ).innerHTML,
                datetime: document.getElementById( 'datetime' + ( i + 1 ) ).innerHTML,
                sortkey: document.getElementById( 'datetime' + ( i + 1 ) ).innerHTML,
                selected: ( obj.settings[ i ] == 1 ? true : false ),
                oldindex: i,
                newindex: i
            };

            return entry;
        }.bind( null, this );

    this.sortFun = function( obj, a, b ) 
        { 
            if ( !a.selected &&  b.selected ) 
                return b.newindex <  obj.threshold ?  1 : -1;
            if (  a.selected && !b.selected ) 
                return b.newindex >= obj.threshold ? -1 :  1;

            return 0;
        }.bind( null, this );

    //$( this ).bind( "yell", function(){
        //alert("In");
    //});
}

albumMover.prototype = 
{
    ctrlmove: function( idx )
    {
        count = $('*').filter(function () {
            return this.id.match(/img\d+/); //regex for the pattern "img followed by a number"
        }).length;

        function selection( a, b )
        {
            if ( !a.selected &&  b.selected ) 
                return 1;
            if (  a.selected && !b.selected ) 
                return -1;
            return 0;
        }

        var selcount = 0;

        var obj = this;

        this.content = Array.apply( null, Array( count ) ).map( this.mapFun );

        this.content.sort( selection );

        for ( i = 0; i < count; i++ ) {

            selcount += this.content[ i ].selected ? 1 : 0;
            this.content[ i ].newindex = i;
        }

        this.threshold = idx + selcount - 1;

        this.content.sort( this.sortFun );

        for ( i = 0; i < count; i++ ) {

            this.settings[ i ] = this.content[ i ].selected ? 1 : 0;

            var oldIndex = this.content[ i ].oldindex;

            if ( i != oldIndex ) {

                var str = this.content[ i ].html;
                str = str.replace( /((file-number|dir-number|piclink|picture|art_keywords|header)_)[0-9]+/g, "$1" + ( i + 1 ) );
                document.getElementById( 'div_' + ( i + 1 ) ).innerHTML = str;

                var str = this.content[ i ].keys;
                document.getElementById( 'keywords' + ( i + 1 ) ).innerHTML = str;

                str = this.content[ i ].datetime;
                document.getElementById( 'datetime' + ( i + 1 ) ).innerHTML = str;

                $( "#picture_" + ( i + 1 ) ).attr("style", 
                    "border:2px solid " 
                  + ( ( this.settings[ i ] ) == 0 ? "white;" : "green;" )
                    );

                if ( this.content[ i ].selected ) {

                    $.ajax({
                        method: 'POST',
                        url: 'newlist.php',
                        data: 
                        {
                            path:     this.picdir,
                            moveFrom: oldIndex,
                            moveTo:   i
                        }
                    });
                }
            }
        }
    },
    move: function( idx, evt )
    {
        count = $('*').filter(function () {
            return this.id.match(/div_\d+/); //regex for the pattern "img followed by a number"
        }).length;

        if ( idx > 0 && idx <= count ) {

            if ( null != evt ) {
                if ( evt.ctrlKey ) {
                    this.ctrlmove( idx );    
                }
            }

            if ( idx != index ) {

                var ob = objs[ index ]
                var id = ob.id

                ob.classList.remove( 'focussed' );

                $( '#' + id ).goStop();

                index = idx;

                id = id.replace( 'picture', 'div' );

                $( '#' + id ).goTo();
                $( '#' + id ).focus();

                //this.navigate( index, evt );

                ob = objs[ index ]

                ob.classList.add( 'focussed' );
            }

            if ( null != evt ) {
                if ( evt.ctrlKey ) {
                    $(window).trigger('resize');
                }

                evt.preventDefault();
            }
        }
    },
    set: function( idx, evt ) 
    {
        if ( idx > 0 && idx <= count ) {

            index = idx;

            if ( 0 == this.settings[ index - 1 ] ) {

                this.settings[ index - 1 ] = 1;
                this.navigate( index, evt );
            }
            else
                this.settings[ index - 1 ] = 0;
        }
    },
    setByFunctor: function( fnc ) 
    {
        try {

            var count = this.settings.length;

            this.content = Array.apply( null, Array( count ) ).map( fnc );

            for ( i = 0; i < count; i++ ) {

                this.settings[ i ] = ( this.content[ i ].selected == true ) ? 1 : 0;

                $( "#picture_" + i ).attr("style", 
                    "border:2px solid " 
                  + ( ( this.settings[ i ] ) == 0 ? "white;" : "green;" )
                    );
            }

            for ( i = 0; i < count; i++ ) {

                if ( this.content[ i ].selected == true ) {

                    this.navigate( i + 1, null );
                    break;
                }
            }
        }
        catch ( err ) {
            console.log( err.message );
        }
    },
    setDirectory: function( directoryName ) 
    {
    }
};

// Setup the prototype chain the right way
extend( mover, albumMover );
