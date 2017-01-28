import React, { Component, PropTypes} from 'react'
import RadioButton from './../components/RadioButton'
import Dropdown from './../components/Dropdown'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import NPECheck from './../util/NPECheck'
import Msg from './../components/Msg'

const typeKey = 'osType';
const bucketKey = 'osBucket';
const endpointKey = 'osEndpoint';
const accessKey = 'osCredKey';
const secretKey = 'osCredSecret';
const prefixKey = 'osPathPrefix'
const diskRootKey = 'osDiskRoot';

export default class StorageSettings extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		this.context.actions.listRegionsForStorageCredentials();
	}
	renderChooseStorageType(){
		return (
			<div className="FlexRow RowPadding">
				<div className="Column">
					<RadioButton onClick={() => this.context.actions.updateStorageCreds(typeKey, 'S3', true)} 
								 isChecked={NPECheck(this.props, `settings/storage/storageCreds/${typeKey}`, '') == 'S3'}
								 label="Amazon S3" />
				</div>
				<div className="Column">
					<RadioButton onClick={() => this.context.actions.updateStorageCreds(typeKey, 'DISK', true)} 
								 isChecked={NPECheck(this.props, `settings/storage/storageCreds/${typeKey}`, '') == 'DISK'}
								 label="File System" />
				</div>
			</div>
		);
	}
	renderStorageSettings(){
		let type = NPECheck(this.props, `settings/storage/storageCreds/${typeKey}`, 'S3');

		let s3Inputs = [
			{
				label: 'Bucket',
				key: bucketKey,
				type: 'text',
				placeholder: 'Enter Bucket Name',
			},
			{
				label: 'Region',
				key: endpointKey,
				type: 'text',
				placeholder: 'Select Region',
				render: this.renderSelectRegion
			},
			{
				label: 'AWS Access Key',
				key: accessKey,
				type: 'text',
				placeholder: 'Enter Access Key'
			},
			{
				label: 'AWS Secret Key',
				key: secretKey,
				type: 'password',
				placeholder: 'Enter Secret Key'
			},
			{
				label: 'Path Prefix',
				key: prefixKey,
				type: 'text',
				placeholder: 'Enter Bucket Prefix'
			}
		];

		let diskInputs = [
			{
				label: 'Storage Root Directory',
				key: diskRootKey,
				type: 'text',
				placeholder: 'Enter Storage Root Directory'
			}
		];

		switch(type){
			case 'S3':
				return (
					<div>
						{s3Inputs.map((inputConfig, index) => this.renderInput(inputConfig, index))}
					</div>
				);
			break;

		   case 'DISK':
		   	return (
		   		<div>
					{diskInputs.map((inputConfig, index) => this.renderInput(inputConfig, index))}
				</div>
		   	);
		}
	}
	renderInput(inputConfig, index){
		if(inputConfig.render && typeof inputConfig.render == 'function') {
			return (
				<div key={index} className="InputRow">
					{inputConfig.render.call(this, inputConfig)}
				</div>
			);
		}
		return (
			<div key={index} className="InputRow">
				<label>{inputConfig.label}</label>
				<input className="BlueBorder FullWidth" 
				       value={NPECheck(this.props, `settings/storage/storageCreds/${inputConfig.key}`, '')}
				       onChange={(e) => this.context.actions.updateStorageCreds(inputConfig.key, e)} 
				       placeholder={inputConfig.placeholder}
				       type={inputConfig.type} />
			 </div>
		);
	}
	renderSelectRegion(inputConfig){		
		let regionValue = NPECheck(this.props, `settings/storage/storageCreds/${endpointKey}`);
		let providerRegions = NPECheck(this.props, 'settings/storage/regions', []);
		let regions = [];

		if(providerRegions.length) {
			regions = providerRegions;
		}

		return (
			<div>
				<label>{inputConfig.label}</label>
				<Dropdown isOpen={NPECheck(this.props, 'settings/storage/regionDropDownIsOpen', false)}
						  toggleOpen={() => this.context.actions.toggleSelectRegionForStorageCredentialsDropDown()}
						  listItems={regions} 
						  renderItem={(region, index) => this.renderRegionItem(region, index)}
						  inputPlaceholder={inputConfig.placeholder}
						  inputClassName="BlueBorder FullWidth"
						  inputValue={regionValue || 's3://'} 
						  noItemsMessage="No Regions Found."/>
			</div>
		);
	}
	renderRegionItem(r, index){
		return (
			<div key={index} className="ListItem" onClick={() => this.context.actions.updateStorageCreds(endpointKey, r.regionCode, true)}>
				<div className="Flex1">{r.displayName}</div>
				<div className="Flex1">{r.regionCode}</div>
			</div>
		);
	}
	renderError(){
		let error = NPECheck(this.props, 'settings/storage/error', false);

		if(error) {
			return (
				<Msg text={error} 
   				 	 style={{margin: '1rem 0 0'}}
   				 	 close={() => this.context.actions.clearStorageError()}/>
			);
		}
	}
	renderSaveButton(){
		if(NPECheck(this.props, 'settings/storage/saveStorageXHR', false)) {
			return (
				<Loader />
			);
		}

		return (
			<Btn onClick={() => this.context.actions.saveStorageSettings()}
				 text="Save" 
				 canClick={true} 
				 style={{width: '200px', margin: '28px auto'}}/>
		);
	}
	render(){
		return (
			<div className="StorageSettings">
				<div className="Title">
					Configure Storage
				</div>
				{this.renderChooseStorageType()}
				{this.renderStorageSettings()}
				{this.renderError()}
				{this.renderSaveButton()}
			</div>
		);
	}
}


StorageSettings.propTypes = {

}

StorageSettings.contextTypes = {
	router: PropTypes.object,
	actions: PropTypes.object
};