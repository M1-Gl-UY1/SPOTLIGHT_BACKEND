// models/ReportUser.js
const { DataTypes } = require('sequelize');

module.exports = (sequelize) => {
  const ReportUser = sequelize.define('ReportUser', {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true
    },
    reason: {
      type: DataTypes.TEXT,
      allowNull: false
    },
    status: {
      type: DataTypes.ENUM('pending', 'reviewed', 'resolved', 'dismissed'),
      defaultValue: 'pending'
    },
    severity: {
      type: DataTypes.ENUM('low', 'medium', 'high', 'critical'),
      defaultValue: 'medium'
    },
    reporterId: {
      type: DataTypes.INTEGER,
      allowNull: false,
      references: {
        model: 'users',
        key: 'id'
      }
    },
    reportedUserId: {
      type: DataTypes.INTEGER,
      allowNull: false,
      references: {
        model: 'users',
        key: 'id'
      }
    }
  }, {
    tableName: 'report_users',
    timestamps: true
  });


  ReportUser.associate = (models) => {
      ReportUser.belongsTo(models.User, { foreignKey: 'reporterId', as: 'reporter' });
      ReportUser.belongsTo(models.User, { foreignKey: 'reportedUserId', as: 'reportedUser' });
  };


  return ReportUser;
};