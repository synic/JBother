<?php

$realm = 'jabber.yourdomain.net'; // change to something slightly meaningful
$chat_service = 'private.jabber.yourdomain.net'; // change to your mu-conf room service
$room_name = "theroomname"; // change to your room name
$room_password = "hard2guess"; // change to anything hard to guess

$name_prefix = 'whatever_'; // change to something slightly meaningful
$jabber_dbhost = 'localhost'; // your jabberd2 MySQL database host
$jabber_dbname = 'jabberd2'; // your jabberd2 MySQL database name
$jabber_dbuser = 'jabberd2'; // your jabberd2 MySQL database user
$jabber_dbpasswd = 'nottherealpassword'; // your jabberd2 MySQL user password

function gen_rand_string($hash)
{
	$chars = array( 'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F', 'g', 'G', 'h', 'H', 'i', 'I', 'j', 'J',  'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O', 'p', 'P', 'q', 'Q', 'r', 'R', 's', 'S', 't', 'T',  'u', 'U', 'v', 'V', 'w', 'W', 'x', 'X', 'y', 'Y', 'z', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0');
	
	$max_chars = count($chars) - 1;
	srand( (double) microtime()*1000000);
	
	$rand_str = '';
	for($i = 0; $i < 8; $i++)
	{
		$rand_str = ( $i == 0 ) ? $chars[rand(0, $max_chars)] : $rand_str . $chars[rand(0, $max_chars)];
	}

	return ( $hash ) ? md5($rand_str) : $rand_str;
}

if (!function_exists('file_put_contents'))
{
  function file_put_contents($filename, $data, $file_append = false)
  {
   $fp = fopen($filename, (!$file_append ? 'w+' : 'a+'));
   if(!$fp)
   {
     trigger_error('file_put_contents cannot write in file.', E_USER_ERROR);
     return;
   }
   fputs($fp, $data);
   fclose($fp);
  }
}

// *****************************************************************************************
// start of stuff for phpBB linkage

define('IN_PHPBB', true);
$phpbb_root_path = '../bb/';
include($phpbb_root_path . 'extension.inc');
include($phpbb_root_path . 'common.'.$phpEx);

$userdata = session_pagestart($user_ip, PAGE_PROFILE);
init_userprefs($userdata);

// session id check
if (!empty($HTTP_POST_VARS['sid']) || !empty($HTTP_GET_VARS['sid']))
{
	$sid = (!empty($HTTP_POST_VARS['sid'])) ? $HTTP_POST_VARS['sid'] : $HTTP_GET_VARS['sid'];
}
else
{
	$sid = '';
}

$script_name = preg_replace('/^\/?(.*?)\/?$/', '\1', trim($board_config['script_path']));
$script_name = ( $script_name != '' ) ? $script_name . '/profile.'.$phpEx : 'profile.'.$phpEx;
$server_name = trim($board_config['server_name']);
$server_protocol = ( $board_config['cookie_secure'] ) ? 'https://' : 'http://';
$server_port = ( $board_config['server_port'] <> 80 ) ? ':' . trim($board_config['server_port']) . '/' : '/';

$server_url = $server_protocol . $server_name . $server_port . $script_name;

//
// End page specific functions
// ---------------------------

if ( !$userdata['session_logged_in'] )
{
	redirect(append_sid("login.$phpEx?redirect=../jbother/launch_chat.$phpEx", true));
}

// end of stuff for phpBB linkage
// *****************************************************************************************

$pass_text = gen_rand_string(false);
$pass_key = "JBother rules!";
$pass_result = "";

while (strlen($pass_key) < strlen($pass_text))
{
	$pass_key .= $pass_key;
}

$pass_key = substr( $pass_key, 0, strlen( $pass_text ));

for ($i = 0; $i < strlen($pass_text); $i++)
{
	$e = ord(substr($pass_text, $i, 1)) ^ Ord(substr($pass_key, $i, 1));
	$pass_result .= " " . dechex($e); // sprintf("%02x", dechex($e));
}
$pass_result = substr($pass_result, 1);

// Make the JABBER database connection.
$jabdb = new sql_db($jabber_dbhost, $jabber_dbuser, $jabber_dbpasswd, $jabber_dbname, false);
if(!$jabdb->db_connect_id)
{
   message_die(CRITICAL_ERROR, "Could not connect to the jabber database");
}

$jabuser = $name_prefix.$userdata['username'].'_'.$userdata['user_id'];

$sql = "select count(*) as z from `authreg` where `username` = '".$jabuser
	."' and `realm` = '".$realm."'";
if ( !($result = $jabdb->sql_query($sql)) )
{
	message_die(GENERAL_ERROR, "Couldn't query if user account exists.", "", __LINE__, __FILE__, $sql);
}
$row = $jabdb->sql_fetchrow($result);

if ($row['z'] == 0)
{
	$sql = "INSERT INTO `active` ( `collection-owner`, `object-sequence` )
		VALUES ( '".$jabuser."@".$realm."', 0);";

	if ( !($result = $jabdb->sql_query($sql)) )
	{
		message_die(GENERAL_ERROR, "Couldn't insert user/update password.", "", __LINE__, __FILE__, $sql);
	}

	$sql = "INSERT INTO `authreg` ( `username`, `realm`, `password` )
		VALUES ( '".$jabuser."', '".$realm
		."', aes_encrypt('".$pass_text."','".$jabber_dbpasswd."') );";
}
else
{
	$sql = "UPDATE `authreg` SET `password` = aes_encrypt('".$pass_text
		."','".$jabber_dbpasswd."') WHERE `username` = '".$jabuser
		."' AND `realm` = '".$realm."'";
}

if ( !($result = $jabdb->sql_query($sql)) )
{
	message_die(GENERAL_ERROR, "Couldn't insert user/update password.", "", __LINE__, __FILE__, $sql);
}

$jabdb->sql_close();

foreach (glob("jnlp/".$jabuser."_*.jnlp") as $outfile)
{
  if ((filemtime($outfile) + 900) <= time())
  {
    unlink($outfile);
  }
}

$data = file_get_contents("template.jnlp") or ($data = "x0YgnRp");
if ($data == "x0YgnRp")
{
        header("Content-type: text/html\n\n");
        print "<B>Error reading template file: Sorry, I guess it's not available right now.</B><BR>";
        exit(0);
}
else if ($data === false)
{
        header("Content-type: text/html\n\n");
        print "<B>Error reading template file: Sorry, I guess it's not available right now.</B><BR>";
        exit(0);
}
$data = str_replace('%roomname%', $room_name, $data);
$data = str_replace('%nickname%', $userdata['username'], $data);
$data = str_replace('%username%', $jabuser, $data);
$data = str_replace('%password%', $pass_result, $data);
$data = str_replace('%roompass%', $room_password, $data);
$data = str_replace('%roomservice%', $chat_service, $data);
$jnlprand = gen_rand_string(false);
$jnlpfile = "jnlp/".$jabuser."_".$jnlprand.".jnlp";
$data = str_replace('%jnlpfile%', $jnlpfile, $data);
file_put_contents($jnlpfile, $data);
header("Location: ".$jnlpfile."\n\n");

?>
