
////////////////////////////////////////////////////////////////
// Scroll plugin

(function($)
{
    $.fn.goTo = function() {

        var top = $(this).offset().top;
        var scroll = $('#paneright').scrollTop();

        console.log( 'Scrolling ' + top );
        console.log( 'Scrolled ' + scroll );

        $('html, body, #paneright').animate({
            scrollTop: top
        }, 'fast' );

        return this; // for chaining...
    }
})(jQuery);

(function($)
{
    $.fn.goStop = function() {

        $('html, body, #paneright').stop();
        return this; // for chaining...
    }
})(jQuery);

function extend(base, sub) {
  // Avoid instantiating the base class just to setup inheritance
  // Also, do a recursive merge of two prototypes, so we don't overwrite 
  // the existing prototype, but still maintain the inheritance chain
  // Thanks to @ccnokes
  var origProto = sub.prototype;
  sub.prototype = Object.create(base.prototype);
  for (var key in origProto)  {
     sub.prototype[key] = origProto[key];
  }
  // The constructor property was set wrong, let's fix it
  Object.defineProperty(sub.prototype, 'constructor', { 
    enumerable: false, 
    value: sub 
  });
}

////////////////////////////////////////////////////////////////
// Polymorphic navigator

function mover() 
{
    var settings;
    var mapFun;
    var sortFun;
    var threshold;
};

mover.prototype = {

  navigate: function( idx, evt ) {
    this.move( idx, evt );
  },
  select: function( idx, evt ) {
    this.set( idx, evt );
  },
  setFunction: function( fnc ) {
    this.setByFunctor( fnc );
  },
  setDirName: function( directoryName ) {
    this.setDirectory( directoryName );
  },
  ctrlmove: function( idx ) {
    // Abstract
  },
  move: function( idx, evt ) {
    // Abstract
  },
  set: function( idx, set ) {
    // Abstract
  },
  setDirectory: function( directoryName ) {
    // Abstract
  },
  setByFunctor: function( fnc ) {
    // Abstract
  }
};

// Here is where the albumMover (and mover) constructors are called
var imgnav;
