const sequelize = require("../sequelize");
const UserModel = require("./User");
const ProviderModel = require("./Provider");
const ReportUserModel = require("./ReportUser");

const models = {};

models.User = UserModel(sequelize);
models.Provider = ProviderModel(sequelize);
models.ReportUser = ReportUserModel(sequelize);

// Associations ---
models.User.hasMany(models.Provider, { foreignKey: "userId", onDelete: "CASCADE" });
models.Provider.belongsTo(models.User, { foreignKey: "userId" });

// ReportUser relations
models.ReportUser.belongsTo(models.User, { foreignKey: "reporterId", as: "reporter" });
models.ReportUser.belongsTo(models.User, { foreignKey: "reportedUserId", as: "reportedUser" });

module.exports = { sequelize, models };
