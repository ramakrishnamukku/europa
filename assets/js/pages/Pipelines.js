import React, {Component, PropTypes} from 'react'
import { Link } from 'react-router'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import BtnGroup from './../components/BtnGroup'
import ControlRoom from './../components/ControlRoom'
import PipelineStageItem from './../components/PipelineStageItem'
import CenteredConfirm from './../components/CenteredConfirm'

export default class Pipelines extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }
  componentDidMount() {
    this.context.actions.listPipelines();
  }
  renderNoPipelines() {
    return (
      <div className="ContentContainer">
        <div className="NoContent">
          <h3>
            You have not created a Pipeline
          </h3>
          <Btn className="LargeBlueButton"
               onClick={ this.context.actions.toggleInitNewPipeline }
               text="Add Pipeline"
               canClick={true} />
        </div>
      </div>
    );
  }
  renderNoPipelinesFound() {
    return (
      <div className="ContentContainer">
        <div className="NoContent">
          <h3>
            No Pipelines found
          </h3>
        </div>
      </div>
    );
  }
  renderNewPipeline() {
    if (this.props.pipelinesStore.initNewPipeline) {
      return (
        <div style={ {margin: "14px 0 0"} }>
          <ControlRoom renderBodyContent={ this.newPipelineForm.bind(this) } />
        </div>
      );
    }
  }
  inputClassName(selector) {
    let hasSelector = this.props.pipelinesStore.newPipelineTemplate.errorFields.includes(selector)
    let className = "BlueBorder FullWidth";
    if (hasSelector) {
      className = "BlueBorder FullWidth Error";
    }

    return className;
  }
  newPipelineForm() {
    return (
      <div>
        <div className="CR_Header">
          <span className="CR_HeaderTitle">
            New Pipeline
          </span>
          <span className="CR_HeaderClose">
            <i className="icon-dis-close"
               onClick={ this.context.actions.toggleInitNewPipeline } />
          </span>
        </div>
        <div className="CR_BodyContent">
          <div className="Flex1">
            <label className="small FlexColumn">
              Pipeline name
            </label>
            <input className={this.inputClassName("name")}
                   style={ {background: "#fff"} }
                   value={this.props.pipelinesStore.newPipelineTemplate[name]}
                   placeholder="Enter Pipeline name..."
                   onChange={(e) => this.context.actions.updateNewPipelineTemplate("name", e.target.value)} />
          </div>
          <div className="Flex1">
            {this.renderNewPipelineConfirm()}
          </div>
        </div>
      </div>
    );
  }
  renderNewPipelineConfirm() {
    if (this.props.pipelinesStore.newPipelineXHR) {
      return (
        <div className="PageLoader">
          <Loader />
        </div>
      );
    }

    return (
      <CenteredConfirm confirmButtonText="Create"
                       noMessage={true}
                       confirmButtonStyle={{}}
                       onConfirm={ this.context.actions.createPipeline }
                       onCancel={this.context.actions.toggleInitNewPipeline } />
    );
  }
  renderPipelineList() {
    if (this.props.pipelinesStore.initNewPipeline) return;

    const store = this.props.pipelinesStore;
    let pipes = store.pipelines || [];

    if (store.filteredPipelines) {
      pipes = store.filteredPipelines;
      if (pipes.length == 0) {
        return this.renderNoPipelinesFound();
      }
    } else if (pipes.length == 0) {
      return this.renderNoPipelines();
    }

    return (
      <div>
        {pipes.map((pipeline, idx) => {
          return (
            <div className="PipelinesListItem"

                 key={pipeline.id}>
              <span>
                <i className="icon-dis-pipeline" />
              </span>
              <span onClick={ () => this.context.router.push(`/pipelines/${pipeline.id}`) }
                    style={ { color: "#1DAFE9"} }>
                {pipeline.name}
              </span>
              <span>
                <i className="icon-dis-trash"
                   onClick={ () => this.context.actions.removePipeline({pipelineId: pipeline.id}) } />
              </span>
            </div>
          );
        }, this)}
      </div>
    );
  }
  renderSearchPipelines() {
    if (this.props.pipelinesStore.initNewPipeline) return;

    return (
      <input className="BlueBorder Search"
             placeholder="Search by name..."
             onChange={ (e) => this.context.actions.filterPipelines(e.target.value) } />
    );
  }
  render() {
    if (this.props.pipelinesStore.pipelinesXHR) {
      return (
        <div className="PageLoader">
          <Loader />
        </div>
      );
    }

    const buttons = [
      {
        icon: 'icon icon-dis-pipeline',
        toolTip: 'New Pipeline',
        onClick: this.context.actions.toggleInitNewPipeline,
        isActive: this.props.pipelinesStore.initNewPipeline
      }
    ]

    return (
      <div className="ContentContainer">
        <div className="PageHeader">
          <h2>
             Pipelines
          </h2>
          <div className="FlexRow">
            <div className="Flex1">
              <BtnGroup buttons={buttons} />
            </div>
          </div>
        </div>
        <div>
          {this.renderNewPipeline()}
          {this.renderSearchPipelines()}
          {this.renderPipelineList()}
        </div>
      </div>
    );
  }
}

Pipelines.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

Pipelines.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};