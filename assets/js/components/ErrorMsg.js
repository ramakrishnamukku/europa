import React, {Component} from 'react'


export default class ErrorMsg extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	render(){
		return (
			<p className="ErrorMsg" style={this.props.style}>
				{(this.props.text) ? this.props.text : ''}
			</p>
		);
	}
}


ErrorMsg.PropTypes = {
	style: React.PropTypes.object,
	text: React.PropTypes.string
};

