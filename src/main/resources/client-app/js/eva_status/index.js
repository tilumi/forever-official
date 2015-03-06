window.React = require('react/addons');
window.jQuery = window.$ = require('jquery');
window.DateTimeField = require('react-bootstrap-datetimepicker');
window.Header = require('../components/header');
window.Reflux = require('reflux');
window.Request = require('superagent');
window.Header = require('../components/header');
window.moment = require('moment');
window.ReactBootstrap = require('react-bootstrap');
window.Table = ReactBootstrap.Table;

Actions = Reflux.createActions({
    "load": {children: ["completed", "failed"]},
    "onStartDateChange": {},
    "onEndDateChange": {},
    "setStartDate": {},
    "setEndDate": {}
})

Actions.load.listen(function () {
    Request.get('/rest/evaStatus/index.html/', function (res) {
        var evaStatusList = JSON.parse(res.text);
        Actions.load.completed(evaStatusList);
    });
});

EVA_STATUS_TABLE = React.createClass({
    mixins: [Reflux.listenTo(Actions.load.completed, "onLoadCompleted"), Reflux.listenTo(Actions.setStartDate, "onStartDateChange"), Reflux.listenTo(Actions.setEndDate, "onEndDateChange"), Reflux.listenTo(Actions.onStartDateChange, "onStartDateChange"), Reflux.listenTo(Actions.onEndDateChange, "onEndDateChange")],
    getInitialState: function () {
        var Level = function (id, label, min, max) {
            this.id = id;
            this.label = label;
            this.min = min;
            this.max = max;
        };
        Level.prototype.isLevel = function (evaStatus) {
            if (evaStatus.lessons.length >= this.min && evaStatus.lessons.length <= this.max) {
                return true;
            }
            return false;
        };
        var Depart = function (id, label, f) {
            this.id = id;
            this.label = label;
            this.isDepartFunction = f;
        };

        Depart.prototype.isDepart = function (evaStatus) {
            return this.isDepartFunction(evaStatus);
        }

        return {
            levels: [
                new Level(1, '連結中 (0 ~ 0課)', 0, 0),
                new Level(2, '入門 (1 ~ 5課)', 1, 5),
                new Level(3, '初級 (6 ~ 15課)', 6, 15),
                new Level(4, '中級 (16 ~ 25課)', 16, 25),
                new Level(5, '高級 (26 ~ 30課)', 26, 30)],
            departs: [new Depart(1, 'SS 男', function (evaStatus) {
                return evaStatus.depart == '國高中部' && evaStatus.gender;
            })
                , new Depart(2, 'SS 女', function (evaStatus) {
                    return evaStatus.depart == '國高中部' && !evaStatus.gender;
                })
                , new Depart(3, 'Campus 男', function (evaStatus) {
                    return evaStatus.depart == '大學部' && evaStatus.gender;
                })
                , new Depart(4, 'Campus 女', function (evaStatus) {
                    return evaStatus.depart == '大學部' && !evaStatus.gender;
                })
                , new Depart(5, '青年部 男', function (evaStatus) {
                    return evaStatus.depart == '青年部' && evaStatus.gender;
                })
                , new Depart(6, '青年部 女', function (evaStatus) {
                    return evaStatus.depart == '青年部' && !evaStatus.gender;
                })
                , new Depart(7, '長年部 男', function (evaStatus) {
                    return evaStatus.depart == '長年部';
                })

            ],
            evaStatusList: []
        };
    },
    onLoadCompleted: function (evaStatusList) {
        //this.evaStatusList = evaStatusList;
        evaStatusList.sort(function (a, b) {
            return a.order - b.order;
        });
        this.setState({evaStatusList: evaStatusList});
    }
    ,
    onStartDateChange: function (startDate) {
        this.setState({startDate: startDate});
    }
    ,
    getLessonsBeforePeriod: function (evaStatus) {
        var result = 0;
        if(this.state.startDate && this.state.endDate && evaStatus.lessons) {
            $.each(evaStatus.lessons, function (lesson) {

                if (moment(lesson[1]).isBefore(this.state.startDate)) {
                    result = result + 1;
                }
            }.bind(this));
        }
        return result;
    },
    getLessonsAfterPeriod: function (evaStatus) {
        var result = 0;
        if(this.state.startDate && this.state.endDate && evaStatus.lessons) {
            $.each(evaStatus.lessons, function (lesson) {
                if (moment(lesson[1]).isAfter(this.state.endDate)) {
                    result = result + 1;
                }
            }.bind(this));
        }
        return result;
    },
    getLessonsBetweenPeriod: function (evaStatus) {
        var result = 0;

        if(this.state.startDate && this.state.endDate && evaStatus.lessons) {
            $.each(evaStatus.lessons, function (lesson) {
                if (moment(lesson[1]).isSame(this.state.endDate) || moment(lesson[1]).isSame(this.state.startDate) || (moment(lesson[1]).isAfter(this.state.startDate) && moment(lesson[1]).isBefore(this.state.endDate))) {
                    result = result + 1;
                }
            }.bind(this));
        }
        return result;
    },
    getListeningEvaStatusBetweenPeriod: function(evaStatusList){
        console.log(evaStatusList);
        var result = 0;
        $.each(evaStatusList, function(evaStatus){
            if(this.getLessonsBetweenPeriod(evaStatus) > 0){
                result = result+1;
            }
        }.bind(this));
        return result;
    },
    onEndDateChange: function (endDate) {
        this.setState({endDate: endDate});
    }
    ,
    groupBy: function (array, f) {
        var groups = {};
        array.forEach(function (o) {
            var group = JSON.stringify(f(o));
            groups[group] = groups[group] || [];
            groups[group].push(o);
        });
        return groups;
    }
    ,
    render: function () {

        return (<Table striped bordered condensed hover>
            <thead>
                <tr>
                    <th></th>
                {this.state.levels.map(function (level) {
                    return (
                        <th>{level.label}</th>
                    )
                })
                    }
                </tr>
            </thead>
            <tbody>
                {
                    this.state.departs.map(function (depart) {
                        return (<tr>
                            <td>{depart.label}</td>
                            {this.state.levels.map(function (level) {
                                var matchedEvaStatusList =  $.grep(this.state.evaStatusList, function (evaStatus) {
                                    return depart.isDepart(evaStatus) && level.isLevel(evaStatus)
                                });
                                return <td><div>{matchedEvaStatusList.length}({this.getListeningEvaStatusBetweenPeriod(matchedEvaStatusList)})</div>{
                                    matchedEvaStatusList.map(function (evaStatus) {
                                        return <div>{evaStatus.name}({this.getLessonsBeforePeriod(evaStatus)} + {this.getLessonsBetweenPeriod(evaStatus)} + {this.getLessonsAfterPeriod(evaStatus)})</div>
                                    }.bind(this))
                                    }</td>
                            }.bind(this))}
                        </tr>);
                    }.bind(this))
                    }
            </tbody>
        </Table>);
    }
})
;

var StartDate = React.createClass({
    mixins: [Reflux.listenTo(Actions.setStartDate, "onStartDateChange")],
    getInitialState: function () {
        return {startDate: ''}
    },
    onStartDateChange: function (startDate) {
        this.setState({startDate: startDate});
    },
    render: function () {
        return <DateTimeField
            inputFormat="YYYY/MM/DD"
            dateTime={this.state.startDate}
            format='YYYY/MM/DD'
            showToday={true}
            onChange = {function (date) {
                Actions.onStartDateChange(date);
            }
                }/>;
    }

});

var EndDate = React.createClass({
    mixins: [Reflux.listenTo(Actions.setEndDate, "onEndDateChange")],
    getInitialState: function () {
        return {endDate: ''}
    },
    onEndDateChange: function (endDate) {
        this.setState({endDate: endDate});
    },
    render: function () {
        return <DateTimeField
            inputFormat="YYYY/MM/DD"
            dateTime={this.state.endDate}
            format='YYYY/MM/DD'
            showToday={true}
            onChange = {function (date) {
                Actions.onEndDateChange(date);
            }
                }
        />;
    }

});

React.render((
    <div>
        <Header/>
        <StartDate/>
        <EndDate/>
        <EVA_STATUS_TABLE/>
    </div>
), document.getElementById('content'));

Actions.load();
Actions.setStartDate(moment().format('YYYY/MM/DD'));
Actions.setEndDate(moment().add(7, 'days').format('YYYY/MM/DD'));
