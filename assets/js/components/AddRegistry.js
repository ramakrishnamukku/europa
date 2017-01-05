/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component} from 'react'
import ContentRow from './../components/ContentRow'
import Loader from './../components/Loader'
import Btn from './../components/Btn'
import Msg from './../components/Msg'
import RadioButton from './../components/RadioButton'
import Dropdown from './../components/Dropdown'
import UploadGCEServiceAccount from './../components/UploadGCEServiceAccount'
import RegistryNames from './../util/RegistryNames'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import NPECheck from './../util/NPECheck'

let provider = 'provider';
let keyName = 'name';
let region = 'region';
let key = 'key';
let secret = 'secret';

export default class AddRegistry extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentWillUnmount() {
		this.context.actions.resetAddRegistryState();	
	}
	addRegistry(){
		this.context.actions.addRegistryRequest().then(() => {
			this.context.actions.toggleShowAddEditRegistryModal();
		});
	}
	inputClassName(selector){
		let hasSelector = this.context.state.addRegistry.errorFields.includes(selector)
		let className;
		if(hasSelector) {
			className = "BlueBorder FullWidth Error";
		} else {
		    className = "BlueBorder FullWidth";
		}

		if(this.props.standaloneMode) {
			className += ' White';
		} else {
			className += ' Dark';
		}

		return className;
	}
	renderRegistryCredentials() {
		return (
			<div className="FlexColumn">
				{this.renderLabel()}
				{this.renderChooseCredsTypes()}
				{this.renderNewRegistryCredentials()}
				{this.renderExistingRegistryCredentials()}
			</div>
		);
	}
	renderLabel(){
		if(!this.props.standaloneMode) {
			return (
				<label>
					Registry Credentials
				</label>
			);
		}
	}
	renderChooseCredsTypes(){
		if(!this.props.standaloneMode) {
			return (
				<div className="FlexRow RowPadding">
					<div className="Column">
						<RadioButton onClick={() => this.context.actions.setNewRepoCredsType('EXISTING')} 
									 isChecked={this.context.state.addRepo.newRepoCredsType == 'EXISTING'}
									 label="Existing Credentials" />
					</div>
					<div className="Column">
						<RadioButton onClick={() => this.context.actions.setNewRepoCredsType('NEW')} 
							  		 isChecked={this.context.state.addRepo.newRepoCredsType == 'NEW'}
									 label="New Credentials" />
					</div>
				</div>
			);
		}
	}
	renderExistingRegistryCredentials() {
		if(this.context.state.addRepo.newRepoCredsType == 'EXISTING' && !this.props.standaloneMode) {

			let selectedRegistryId = NPECheck(this.context.state, 'addRepo/newRepo/repo/credId', null);
			let selectedRegistry, selectedRegistryName = '';

			if(selectedRegistryId) {
				selectedRegistry = this.context.state.registriesMap[selectedRegistryId];
			}

			if(selectedRegistry) {
				selectedRegistryName = `${selectedRegistry.provider} - ${selectedRegistry.name} - ${selectedRegistry.region}`
			}

			return (
				<div className="Flex1">
					<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
						Select Credentials
					</label>
					<Dropdown isOpen={this.context.state.addRepo.selectExistingCredentialsDropdown}
							  toggleOpen={() => this.context.actions.toggleSelectExistingCredsDropdown()}
							  listItems={this.context.state.registries} 
							  renderItem={(reg, index) => this.renderExistingCredentialItem(reg, index)}
							  inputPlaceholder="Select Credentials"
							  inputClassName="BlueBorder FullWidth"
							  inputValue={selectedRegistryName}
							  className="Flex1"
							  XHR={this.context.state.registriesXHR}/>

				</div>
			);
		}
	}
	renderExistingCredentialItem(reg, index){
		let id = reg.id;
		let className = "ListItem FlexRow";

		if(id == NPECheck(this.context.state, 'addRepo/newRepo/repo/credId', null)) {
			className += " Active";
		}		

		return (
			<div key={index} className={className} 
				 onClick={ () => this.context.actions.selectCredsForNewRepo(null, id) }>
				 <div>
					<img src={RegistryProviderIcons(reg.provider)} />
				</div>
				<div className="FlexColumn">
					<span className="Label">
						{RegistryNames[reg.provider]}
					</span>
					<span className="FlexRow">
						<span className="Cell">
							<span className="Label">Name:</span>
							<span className="Value">{reg.name}</span>
						</span>
						<span className="Cell">
							<span className="Label">Region</span>
							<span className="Value">{reg.region}</span>
						</span>
					</span>
				</div>
			</div>
		);
	}
	renderSelectProvider(readOnly, isEdit){
		let providers = Object.keys(RegistryNames);
		let selectedProvider = NPECheck(this.context.state, 'addRegistry/newRegistry/provider', '');
		let selectedProviderName = (selectedProvider) ? RegistryNames[selectedProvider] : '';

		if(isEdit) {
			return (
				<div className="Flex1 FlexRow AlignCenter">
					<img style={{height: '40px', marginRight: '7px'}} src={RegistryProviderIcons(this.context.state.addRegistry.newRegistry[provider])} />
					{selectedProvider}
				</div>
			);
		}

		return (
			<div className="Flex1">
				<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
					Docker Registry Provider
				</label>
				<Dropdown isOpen={this.context.state.addRegistry.selectProviderDropdown}
						  toggleOpen={() => this.context.actions.toggleSelectProviderDropdown()}
						  listItems={providers} 
						  renderItem={(registryProvider, index) => this.renderProviderListItem(registryProvider, index)}
						  inputPlaceholder="Select Provider"
						  inputClassName={this.inputClassName(provider)}
						  inputValue={selectedProviderName}
						  className="Flex1"/>
			</div>
		);
	}
	renderProviderListItem(registryProvider, index){
		let reg = RegistryNames[registryProvider];
		let className = "ListItem FlexRow";

		if(registryProvider == NPECheck(this.context.state, 'addRegistry/newRegistry/provider', null)) {
			className += " Active";
		}		

		return (
			<div key={index} className={className}
			     onClick={() => this.context.actions.updateNewRegistryField(provider, registryProvider, true)}>
				<img src={RegistryProviderIcons(registryProvider)} />
				{reg}
			</div>
		);
	}
	renderInputKeyName(readOnly, isEdit){

		let keyNameValue = this.context.state.addRegistry.newRegistry[keyName];

		if(isEdit) {
			return (
				<div className="Flex1">
					{keyNameValue}
				</div>
			);
		}

		return (
			<div className="Flex1">
				<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
					Key Name
				</label>
				<input className={this.inputClassName(keyName)}
					   value={keyNameValue}
				       placeholder="Enter Key Name.."
					   onChange={(e) => this.context.actions.updateNewRegistryField(keyName, e)} 
					   {...readOnly}/>
			</div>
		);
	}
	renderInputPublicKey(readOnly, isEdit){
		let currentProvider = NPECheck(this.context.state, 'addRegistry/newRegistry/provider', '');
		if(!currentProvider || currentProvider == 'ECR') {
			return (
				<div className="Flex1">
					<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
						Public Key
					</label>
					<input className={this.inputClassName(key)}
						   value={this.context.state.addRegistry.newRegistry[key]}
						   placeholder="Enter Public Key.."
						   onChange={(e) => this.context.actions.updateNewRegistryField(key, e)} />
				</div>
			);
		}
	}
	renderInputPrivateKey(readOnly, isEdit){
		let currentProvider = NPECheck(this.context.state, 'addRegistry/newRegistry/provider', '');
		if(!currentProvider || currentProvider == 'ECR') {
			return (
				<div className="Flex1">
					<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
						Private Key
					</label>
					<input className={this.inputClassName(secret)}
						   value={this.context.state.addRegistry.newRegistry[secret]}
						   placeholder="Enter Private Key.."
						   defaultValue={(this.props.isEdit) ? '******************' : '' }
						   onChange={(e) => this.context.actions.updateNewRegistryField(secret, e)} />
				</div>
			);
		}
	}
	renderSelectRegion(readOnly, isEdit){		
		let regionValue = this.context.state.addRegistry.newRegistry[region]

		if(isEdit) {
			return (
				<div className="Flex1">
					{regionValue}
				</div>
			);
		}

		let regions = [];
		let providerRegions = NPECheck(this.context.state, 'addRegistry/providerRegions', null);

		if(providerRegions.length) {
			regions = providerRegions;
		}

		return (
			<div className="Flex1">
				<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
					Key Region
				</label>
				<Dropdown isOpen={this.context.state.addRegistry.selectRegionDropdown}
						  toggleOpen={() => this.context.actions.toggleSelectRegionDropdown()}
						  listItems={regions} 
						  renderItem={(region, index) => this.renderRegionItem(region, index)}
						  inputPlaceholder="Select Region"
						  inputClassName={this.inputClassName(region)}
						  inputValue={regionValue} 
						  noItemsMessage="Select Provider First"/>
			</div>
		);
	}
	renderRegionItem(r, index){
		return (
			<div key={index} className="ListItem" onClick={() => this.context.actions.updateNewRegistryField(region, r.regionCode, true)}>
				{r.displayName}
			</div>
		);
	}
	renderUploadGCEServiceAccount(){
		if(this.context.state.addRegistry.newRegistry.provider == 'GCR') {

			let isComplete = this.context.state.addRegistry.credentialType == 'SERVICE_CREDENTIAL' 
							&& !!NPECheck(this.context.state, 'addRegistry/newRegistry/secret', false);

			return (
				<UploadGCEServiceAccount handleDrop={(json) => this.context.actions.updateServiceAccountCredential(json)}
										 cancel={() => this.context.actions.cancelServiceAccountCredentialUpload()}
										 isComplete={isComplete}
										 standaloneMode={this.props.standaloneMode}
										 isEdit={this.props.isEdit}/>
			);
		}
	}
	renderNewRegistryCredentials(){
		if(this.context.state.addRepo.newRepoCredsType == 'NEW' || this.props.standaloneMode) {

			let isEdit = this.props.isEdit;
			let className = (isEdit) ? 'AddEditRegistryCreds Edit' : 'AddEditRegistryCreds';
			let readOnly = {};

			if(isEdit) {
				readOnly['readOnly'] = 'readOnly';
				readOnly['disabled'] = 'disabled';
			}

			return (
				<div className="FlexColumn">
					<div className={className} style={this.props.standaloneMode ? {} : {margin: '0 -6px'}}>
						{this.renderSelectProvider(readOnly, isEdit)}
						{this.renderInputKeyName(readOnly, isEdit)}
						{this.renderInputPublicKey(readOnly, isEdit)}
						{this.renderInputPrivateKey(readOnly, isEdit)}
						{this.renderSelectRegion(readOnly, isEdit)}
					</div>
					{this.renderUploadGCEServiceAccount()}
				</div>
			);
		}
	}
	renderErrorMsg(){
		if(this.context.state.addRegistry.errorMsg) {
			return (
				<Msg text={this.context.state.addRegistry.errorMsg} 
					 close={() => this.context.actions.clearAddRegistryError()}/>
			);
		}
	}
	renderSuccessMsg(){
		if(this.context.state.addRegistry.success) {

			let message = `Successfully ${(this.props.isEdit) ? 'updated' : 'added'} registry credentials`;

			return (
				<Msg text={message} 
				     isSuccess={true}
				     close={() => this.context.actions.clearAddRegistrySuccess()}/>
			);
		}
	}
	renderLoader(){
		return (
			<Loader />
		);
	}
	renderActions(){
		return (
			<div className="FlexRow JustifyCenter" style={{margin: '0 auto', width: '300px'}}>
				<div className="Flex1" style={{margin: '0px 10px'}}>
					<Btn onClick={() => this.addRegistry()}
						 text={(this.props.isEdit) ? 'Save Credential' : 'Add Credential'}
						 canClick={this.context.actions.canAddRegistry()} />
					</div>
				<div className="Flex1" style={{margin: '0px 10px'}}>
					<Btn onClick={ () => this.context.actions.toggleShowAddEditRegistryModal() }
						 className="Btn Cancel"
						 text="Cancel"
						 canClick={true} />
				</div>
			</div>
		);
	}
	renderAddRegistry(){
		let rows = [{
			columns: [{
				icon: (this.props.standaloneMode) ? null : 'icon icon-dis-credential',
                renderBody: this.renderRegistryCredentials.bind(this)
            }]
		}, {
			columns: [{
				icon: (this.props.standaloneMode) ? null : 'icon icon-dis-blank',
                renderBody: this.renderErrorMsg.bind(this),
                condition: this.context.state.addRegistry.errorMsg
            }]
		}];

		if(this.props.standaloneMode) {
			let standAloneRows = [{
				columns: [{
					icon: (this.props.standaloneMode) ? null : 'icon icon-dis-blank',
	                renderBody: this.renderLoader.bind(this),
	                condition: this.context.state.addRegistry.XHR
	            }]
			}, {
				columns: [{
					icon: (this.props.standaloneMode) ? null : 'icon icon-dis-blank',
	                renderBody: this.renderSuccessMsg.bind(this),
	                condition: this.context.state.addRegistry.success
	            }]
			}, {
				columns: [{
					icon: (this.props.standaloneMode) ? null : 'icon icon-dis-blank',
	                renderBody: this.renderActions.bind(this),
	                condition: this.props.standaloneMode
	            }]
			}]

			rows = rows.concat(standAloneRows);
		}

		return rows.map(this.renderContentRow.bind(this));
	}
	renderContentRow(row, index){
		return (
			<ContentRow key={index}
						row={row}
						bodyStyle={(this.props.standaloneMode) ? {margin: '0'} : {}} />
		);
	}
	render() {
		return (
			<div className="ContentContainer">
				<div>
					{this.renderAddRegistry()}
				</div>
			</div>
		);
	}
}

AddRegistry.propTypes = {
	standaloneMode: React.PropTypes.bool,
	isEdit: React.PropTypes.bool
};

AddRegistry.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

AddRegistry.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};