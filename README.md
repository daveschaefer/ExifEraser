[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://Tommy-Geenexus.mit-license.org)

# ExifEraser
## JPEG Exif eraser application for Android 6.0+
<a href='https://play.google.com/store/apps/details?id=com.none.tom.exiferaser&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' height='80' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>
### How does it work?
- Exif uses [TIFF](https://de.wikipedia.org/wiki/Tagged_Image_File_Format) to store data
- Exif sits in the APP1 segment and uses the following data structure (shortcut of [this lengthy description](https://www.media.mit.edu/pia/Research/deepview/exif.html)): <br>`0xff 0xd8 SOI Marker`<br>`0xff 0xe1 APP1 Marker`<br>`0x45 0x78 0x69 0x66 0x00 0x00 Exif Header`<br>`...`
- This application detects the presence of the Exif header in JPEG files,<br>and proceeds to strip the entire Exif segment using  [Apache Sanselan](https://github.com/apache/sanselan) internally<br>(without requesting any permissions)
