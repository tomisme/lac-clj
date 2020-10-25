<?php

// TODO
// $uri = $_SERVER['REQUEST_URI'];
// $pieces = explode("/", $uri);
// $res = $pieces[1]."/wp-admin";
// if ($pieces[1] != '') $res = "/".$res;
//
// header("Location: ".$res); /* Redirect browser */
// exit();

// get_header();
?>
<div class="wrapper">
<main>
<?php
if ( have_posts() ) :
    while ( have_posts() ) : the_post();
        the_title( '<h1>', '</h1>' );
        the_content();
    endwhile;
else:
    _e( 'Sorry, no pages matched your criteria.', 'textdomain' );
endif;
?>
</main>
</div>
