import React, { Component, PropTypes} from 'react'
import RadioButton from './../components/RadioButton'
import Dropdown from './../components/Dropdown'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import Msg from './../components/Msg'
import AWSRegions from './../util/AWSRegions'
import NPECheck from './../util/NPECheck'

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

		this.state = {
			isEdit: !(this.props.hasOwnProperty('storage') && this.props.storage == false)
		};
	}
	componentDidMount() {
		if(this.state.isEdit) {
			this.context.actions.getStorageSettings();
		}
	}
	componentWillUnmount() {
		// Avoid race conditions
		setTimeout(() => {
			this.context.actions.resetStorageState();
		});
	}
	saveStorageSettings(){
		this.context.actions.saveStorageSettings()
		.then(() => {
			if(!this.state.isEdit) {
				window.location = '/';
			}
		});
	}
	renderChooseStorageType(){
		return (
			<div className="FlexRow RowPadding">
				<div className="Column">
					<RadioButton onClick={() => this.context.actions.updateStorageCreds(typeKey, 'S3', true)} 
								 isChecked={NPECheck(this.props, `settings/storage/storageCreds/${typeKey}`, '') == 'S3'}
								 disabled={this.state.isEdit}
								 label="Amazon S3" />
				</div>
				<div className="Column">
					<RadioButton onClick={() => this.context.actions.updateStorageCreds(typeKey, 'DISK', true)} 
								 isChecked={NPECheck(this.props, `settings/storage/storageCreds/${typeKey}`, '') == 'DISK'}
								 disabled={this.state.isEdit}
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
				editableOnceSet: false
			},
			{
				label: 'Region',
				key: endpointKey,
				type: 'text',
				placeholder: 'Select Region',
				render: this.renderSelectRegion,
				editableOnceSet: false
			},
			{
				label: 'AWS Access Key',
				key: accessKey,
				type: 'text',
				placeholder: 'Enter Access Key',
				editableOnceSet: true
			},
			{
				label: 'AWS Secret Key',
				key: secretKey,
				type: 'password',
				placeholder: 'Enter Secret Key',
				editableOnceSet: true
			},
			{
				label: 'Path Prefix',
				key: prefixKey,
				type: 'text',
				placeholder: 'Enter Bucket Prefix',
				editableOnceSet: false
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

		let readOnly = {};
		let label = [
			<label key={1} style={(!inputConfig.editableOnceSet) ? {color: '#808285'} : {}}>{inputConfig.label}</label>
		];
		let className = "BlueBorder FullWidth";

		if(this.state.isEdit) {
			className += ' White';

			if(!inputConfig.editableOnceSet) {

				readOnly = {
					readOnly: 'readOnly',
					disabled: 'disabled'
				};

				label.push(
					<label key={2} style={{fontStyle: 'italic', color: '#808285'}}>&nbsp;(This value cannot be changed)</label>
				);

			}
		}

		return (
			<div key={index} className="InputRow">
				<div className="FlexRow">
					{label}
				</div>
				<input className={className}
				       value={NPECheck(this.props, `settings/storage/storageCreds/${inputConfig.key}`, '')}
				       onChange={(e) => this.context.actions.updateStorageCreds(inputConfig.key, e)} 
				       placeholder={inputConfig.placeholder}
				       type={inputConfig.type} 
				       {...readOnly} />
			 </div>
		);
	}
	renderSelectRegion(inputConfig){		
		let regionValue = NPECheck(this.props, `settings/storage/storageCreds/${endpointKey}`);
		let regions = AWSRegions;
		
		let readOnly = false
		let label = [
			<label key={1} style={(this.state.isEdit) ? {color: '#808285'} : {}}>{inputConfig.label}</label>
		];
		let className = "BlueBorder FullWidth";

		if(this.state.isEdit) {
			className += ' White';

			if(!inputConfig.editableOnceSet) {
				readOnly = true;
				label.push(
					<label key={2} style={{fontStyle: 'italic', color: '#808285'}}>&nbsp;(This value cannot be changed)</label>
				);
			}
		}

		return (
			<div className="FlexColumn">
				<div className="FlexRow">
					{label}
				</div>
				<Dropdown isOpen={NPECheck(this.props, 'settings/storage/regionDropDownIsOpen', false)}
						  toggleOpen={() => this.context.actions.toggleSelectRegionForStorageCredentialsDropDown()}
						  listItems={regions} 
						  renderItem={(region, index) => this.renderRegionItem(region, index)}
						  inputPlaceholder={inputConfig.placeholder}
						  inputClassName={className}
						  inputValue={regionValue || 's3://'} 
						  inputReadOnly={readOnly}
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
		let error2 = NPECheck(this.props, 'settings/storage/getError', false);

		if(error || error2) {
			return (
				<Msg text={error || error2} 
   				 	 style={{margin: '1rem 0 0'}}
   				 	 close={() => this.context.actions.clearStorageError()}/>
			);
		}
	}
	renderSaveButton(){
		if(!this.state.isEdit || (this.state.isEdit && NPECheck(this.props, `settings/storage/storageCreds/${typeKey}`, '') == 'S3')) {
			if(NPECheck(this.props, 'settings/storage/saveStorageXHR', false)) {
				return (
					<Loader />
				);
			}

			return (
				<Btn onClick={() => this.saveStorageSettings()}
					 text="Save" 
					 canClick={true} 
					 style={{width: '200px', margin: '28px auto'}}/>
			);
		}
	}
	renderSuccess(){
		if(NPECheck(this.props, 'settings/storage/saveStorageSuccess', false)) {
			return (
				<Msg text="Successfully updated storage credentials" 
				 	 style={{margin: '1rem 0 0'}}
					 isSuccess={true}/>
			);
		}
	}
	render(){

		console.log(this.state.isEdit);
		let className = "StorageSettings";

		if(this.state.isEdit) {
			className += ' Edit';
		}

		if(NPECheck(this.props, 'settings/storage/getXHR', false)) {
			return (
				<div className="PageLoader">
					<Loader />
				</div>
			);
		}

		return (
			<div className={className} style={(this.state.isEdit) ? {} : {marginTop: '28px'}}>
				<div className="Title">
					Configure Storage
				</div>
				{this.renderChooseStorageType()}
				{this.renderStorageSettings()}
				{this.renderError()}
				{this.renderSuccess()}
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