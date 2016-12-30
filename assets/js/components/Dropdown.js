/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, { Component, PropTypes } from 'react'
import Loader from './../components/Loader'

export default class Dropdown extends Component {
	constructor(props) {
		super(props);
		this.state = {}
	}
	componentDidMount() {
		document.body.addEventListener('click', () => {
			if(this.props.isOpen) {
				this.props.toggleOpen();
			}
		});
	}
	componentWillUnmount() {
		
	}
	renderInput(){
		if(this.props.inputPlaceholder || this.props.inputClassName || this.props.filterFn || this.props.inputOnChange || this.props.inputValue) {
			return (
				<input className={this.props.inputClassName}
					   onClick={ () => this.props.toggleOpen() }
					   placeholder={this.props.inputPlaceholder}
					   onChange={this.props.inputOnChange}
					   value={(this.props.isOpen) ? undefined : this.props.inputValue} />
			);
		}
	}
	renderNoItemsMessage(){
		return (
			<div className="Flex1 NoItems">
				{this.props.noItemsMessage || 'No Items'}
			</div>
		);
	}
	renderLoader(){
		return (
			<div className="LoaderContainer">
				<Loader />
			</div>
		);
	}
	renderDropdown(){
		let innerClassName = "Dropdown";

		if(this.props.isOpen) {
			innerClassName += ' Open';
		}

		let filterFn = (this.props.filterFn) ? this.props.filterFn : () => true;
		let listItems = this.props.listItems.filter(filterFn);
		let innerContent = this.renderNoItemsMessage();

		if(listItems.length) innerContent = listItems.map(this.props.renderItem);
		if(this.props.XHR) innerContent = this.renderLoader();
        
		return (
			<div className={innerClassName}>
				{innerContent}
			</div>
		);
	}
	render(){
		let containerClassName = "DropdownContainer";
		

		return (
			<div className={containerClassName}>
				{this.renderInput()}
				{this.renderDropdown()}
			</div>
		);
	}
}

Dropdown.propTypes = {
	isOpen: PropTypes.bool.isRequired,
	toggleOpen: PropTypes.func.isRequired,
	listItems: PropTypes.array.isRequired,
	renderItem: PropTypes.func.isRequired,
	inputValue: PropTypes.string,
	filterFn: PropTypes.func,
	inputOnChange: PropTypes.func,
	inputPlaceholder: PropTypes.string,
	inputClassName: PropTypes.string,
	noItemsMessage: PropTypes.string,
	XHR: PropTypes.bool
};

Dropdown.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};