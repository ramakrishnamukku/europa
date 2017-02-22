# europa
Europa is a new Container Registry that makes it easy for software teams to host docker images within their infrastructure along with a unified view of all their images stored in local and remote repositories.

<img src="http://www-beta.distelli.com/images/europa/europa.png" width="300px"></img>
<br/>
https://www.distelli.com/europa

### Editions

There are three editions of Europa:

- Europa Community Edition (CE) is available freely under the Apache 2.0 license.
- Europa Premium Edition (PE) includes extra features that are useful for small teams
- Europa Enterprise Edition (EE) includes even more feautures (Teams, SAML, Service Accounts) useful for enterprises.

For pricing and support for Europa Premium & Enterprise Editions please visit https://distelli.com/europa.

### Features

<ul>
  <li>Push and pull images, securely, from the privacy of your own network.</li>
  <li>Choose where to store your images from a variety of options, including S3 and local disk.</li>
  <li>Support for Docker v2 API.</li>
  <li>Support for connecting and synchronizing to other external Docker repositories.</li>
  <li>Audit trails for repositories with a history of every action.</li>
  <li>Automated push pipelines allowing the redundant push of images to multiple downstream repositories.</li>
  <li>Single Sign-on via a SAML IDP</li>
  <li>Fine Grained Access Control</li>
  <li>Teams.</li>
</ul>

<table>
  <tr><th><br>Feature</th><th style="text-align:center">Europa<br>Community</th><th style="text-align:center">Europa<br>Premium</th><th style="text-align:center">Europa<br>Enterprise</th></tr>
  <tr><td>Open Source</td>             <td style="text-align:center">Yes</td><td style="text-align:center">.</td><td style="text-align:center">.</td></tr>
  <tr><td>Local Repositories</td>      <td style="text-align:center">Yes</td><td style="text-align:center">Yes</td><td style="text-align:center">Yes</td></tr>
  <tr><td>Remote Repositories</td>     <td style="text-align:center">Yes</td><td style="text-align:center">Yes</td><td style="text-align:center">Yes</td></tr>
  <tr><td>Automated Push Pipelines</td><td style="text-align:center">Yes</td><td style="text-align:center">Yes</td><td style="text-align:center">Yes</td></tr>
  <tr><td>Multi-user Support</td>      <td style="text-align:center">.</td><td style="text-align:center">Yes</td><td style="text-align:center">Yes</td></tr>
  <tr><td>Access Control</td>          <td style="text-align:center">.</td><td style="text-align:center">Yes</td><td style="text-align:center">Yes</td></tr>
  <tr><td>Single Sign-on</td>          <td style="text-align:center">.</td><td style="text-align:center">.</td><td style="text-align:center">Yes</td></tr>
  <tr><td>Teams</td>                   <td style="text-align:center">.</td><td style="text-align:center">.</td><td style="text-align:center">Yes</td></tr>
</table>


### Getting Started

To get started with Europa Community Edition you can pull the latest image from Docker Hub:

`docker pull distelli/europa:latest`

Full documentation and a getting started guide is available at https://www.distelli.com/docs/europa

### Contribute

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute issues, fixes, and patches to this project.

### Support

If any issues are encountered while using Europa Community Edition, several
avenues are available for support:

<table>
<tr>
	<th align="left">
	Issue Tracker
	</th>
	<td>
	https://github.com/distelli/europa/issues
	</td>
</tr>
<tr>
	<th align="left">
	Mailing List
	</th>
	<td>
	europa-community@distelli.com
	</td>
</tr>
</table>

### License

This project is distributed under [Apache License, Version 2.0](LICENSE).
