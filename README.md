HowToHack310BillionDollarCompany

================================

Well again big guys with bad security practices, this time Amazon AWS S3 service.

The problem is the following, Amazon implements since SDK 2.2.5 to 2.2.8 for Android the class CognitoCachingCredentialsProvider, 

this class was created for an type of authentication, but this authentication is very insecure, uses an context, to get the package name, identityPoolId and region; With this insecure authentication you can get, put, modify, delete and list bucket's and files in all S3; Only need extract the identityPoolId and crate a fake app with the same package name of the original app, and ready !!!

Example: 

new CognitoCachingCredentialsProvider(context,"us-east-1:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", Regions.US_EAST_1);

Create fake apps and disassembly this, is really simple and easy, any guy can do it, you can see my old hack

[HowToHack85MillonDolarsCompany](https://github.com/JhetoX/HowToHack85MillonDolarsCompany)<br/>

This test are do with data extracted from iOS App and with this data was created fake Android App, the problem always it, store keys on client side, this is insecure prectice.<br/><br/><br/>


This security flaw was discover by me: Jheto Xekri

You can contact me in:

Profile web: http://about.me/jheto.xekri<br/>
or by Email: jheto.xekri@outlook.com<br/>
or by Whatsapp: +573122844198<br/>
or by Viber: +573122844198<br/>
or by Skype: jheto.xekri<br/>

Donations:

Coinbase: 1NzDu9iuZJPbsyQJxMFtk4YfWPMyVgNea1<br/>
Paypal: jheto.xekri@outlook.com
